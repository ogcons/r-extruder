package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class RScriptService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RProcessor rProcessor;

    @Value("${rscript.path}")
    private String rScriptPath;

    @Value("${rscript.uploadDir}")
    private String uploadDir;


    public byte[] createPlotFromRScript(MultipartFile uploadedFile) throws IOException, InterruptedException {
        try {
            String scriptFileName = saveRScript(uploadedFile);
            byte[] plotBytes = executeRScriptAndRetrievePlot(scriptFileName);
            return documentService.generateWord(plotBytes);
        } catch (IOException | InterruptedException e) {
            log.error("IOException during R script execution: {}", e.getMessage());
            throw e;
        }
    }

    protected String saveRScript(MultipartFile uploadedFile) throws IOException {
        String fileName = uploadedFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        uploadedFile.transferTo(filePath.toFile());
        return fileName;
    }

    protected byte[] executeRScriptAndRetrievePlot(String scriptFileName) throws IOException, InterruptedException {
        var outputFileName = scriptFileName.replace(".R", ".png");

        // Modify script to save the plot as a PNG
        String modifiedScriptContent = modifyScriptContent(scriptFileName, outputFileName);

        // Save modified script as a temp file
        Path modifiedScriptPath = saveModifiedScript(modifiedScriptContent, scriptFileName);

        Path destinationPath = Paths.get(uploadDir, outputFileName);

        try {
            // Execute script with outputFileName and destinationPath parameters
            executeRScript(modifiedScriptPath, outputFileName, destinationPath);

            // Move plot to the same dir as script
            Files.move(Paths.get(outputFileName), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            // Retrieve plot bytes
            return Files.readAllBytes(destinationPath);
        } catch (IOException | InterruptedException e) {
            log.error("Error during R script execution: {}", e.getMessage());

            throw e;
        }
    }

    /**
     * Extends the original R script by code that enables the plotting of PNG files.
     * @param scriptFileName
     * @param outputFileName
     * @return
     * @throws IOException
     */
    protected String modifyScriptContent(String scriptFileName, String outputFileName) throws IOException {
        String scriptContent = Files.readString(Paths.get(uploadDir, scriptFileName));
//TODO %s
        // Modify script to save the plot as a PNG
        if (scriptContent.contains("plot(")) {
            scriptContent = scriptContent.replace("plot(", String.format("png('%s')\nplot(", outputFileName));
            scriptContent += "\ndev.off()";
        }
        if (scriptContent.contains("KinReport(")) {
            int index = scriptContent.indexOf("KinReport(");
            String pngCommand = String.format("png('%s')%s", outputFileName, System.lineSeparator());
            scriptContent = scriptContent.substring(0, index) + pngCommand + scriptContent.substring(index);
        }

        return scriptContent;
    }

    protected Path saveModifiedScript(String modifiedScriptContent, String scriptFileName) throws IOException {
        Path modifiedScriptPath = Path.of(
                uploadDir + File.separator + "modified_" + scriptFileName.replace(" ", "_"));

        Files.write(modifiedScriptPath, modifiedScriptContent.getBytes(), StandardOpenOption.CREATE);
        return modifiedScriptPath;
    }

    protected void executeRScript(Path modifiedScriptPath, String outputFileName, Path destinationPath) throws IOException, InterruptedException {
        String command = rScriptPath + " " + modifiedScriptPath.toAbsolutePath();

        // The external R executable creates a PNG that is named like the script itself
        Process process = rProcessor.execute(command, outputFileName, destinationPath);

        // Capture process output
        String processOutput = IOUtils.toString(process.getInputStream(), String.valueOf(StandardCharsets.UTF_8));
        String processError = IOUtils.toString(process.getErrorStream(), String.valueOf(StandardCharsets.UTF_8));

        // Log process messages
        log.info("Process Output:\n{}", processOutput);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            // Log process errors
            log.error("Process Errors:\n{}", processError);
            throw new IOException("Failed to execute modified R script. Exit code: " + exitCode);
        }
    }
    //TODO cleanUp function
}