package hr.ogcs.rextruderservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
@Slf4j
public class RScriptService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RProcessor rProcessor;

    @Getter
    public String outputFileName;

    @Value("${rscript.path}")
    private String rScriptPath;

    @Value("${rscript.uploadDir}")
    private String uploadDir;


    public byte[] uploadAndExecuteRScript(MultipartFile uploadedFile) {
        try {
            String scriptFileName = uploadRScript(uploadedFile);
            byte[] plotBytes = executeRScriptAndRetrievePlot(scriptFileName);
            return documentService.generateWord(plotBytes);
        } catch (IOException e) {
            log.error("IOException during R script execution: {}", e.getMessage());
            throw new RuntimeException("IOException during R script execution", e);
        }
    }

    protected String uploadRScript(MultipartFile uploadedFile) throws IOException {
        String fileName = uploadedFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        uploadedFile.transferTo(filePath.toFile());
        return fileName;
    }

    public byte[] executeRScriptAndRetrievePlot(String scriptFileName) throws IOException {
        outputFileName = scriptFileName.replace(".R", ".png");

        // Modify script to save the plot as a PNG
        String modifiedScriptContent = modifyScriptContent(scriptFileName, outputFileName);

        // Save modified script as a temp file
        Path modifiedScriptPath = saveModifiedScript(modifiedScriptContent);

        try {
            // Execute script
            executeRScript(modifiedScriptPath);

            // Move plot to the same dir as script
            Path destinationPath = Paths.get(uploadDir, outputFileName);
            Files.move(Paths.get(outputFileName), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // Retrieve plot bytes
            return Files.readAllBytes(destinationPath);
        } catch (Exception e) {
            // Log the exception for troubleshooting purposes
            log.error("Error during R script execution: {}", e.getMessage());

            // Rethrow the exception as IOException
            throw new IOException("Error during R script execution", e);
        }
    }

    /**
     * Extends the original R script by code that enables the plotting of PNG files.
     * @param scriptFileName
     * @param outputFileName
     * @return
     * @throws IOException
     */
    String modifyScriptContent(String scriptFileName, String outputFileName) throws IOException {
        String scriptContent = new String(Files.readAllBytes(Paths.get(uploadDir, scriptFileName)), StandardCharsets.UTF_8);

        // Modify script to save the plot as a PNG
        if (scriptContent.contains("plot(")) {
            scriptContent = scriptContent.replace("plot(", String.format("png('%s')\nplot(", outputFileName));
            scriptContent += "\ndev.off()";
        }
        if (scriptContent.contains("KinReport(")) {
            int index = scriptContent.indexOf("KinReport(");
            String pngCommand = String.format("png('%s')\n", outputFileName);
            scriptContent = scriptContent.substring(0, index) + pngCommand + scriptContent.substring(index);
        }
        return scriptContent;
    }

    Path saveModifiedScript(String modifiedScriptContent) throws IOException {
        Path modifiedScriptPath = Files.createTempFile("modified_script", ".R");
        Files.write(modifiedScriptPath, modifiedScriptContent.getBytes(), StandardOpenOption.CREATE);
        return modifiedScriptPath;
    }

    void executeRScript(Path modifiedScriptPath) throws IOException, InterruptedException {
        String command = rScriptPath + " " + modifiedScriptPath.toAbsolutePath();

        // The external R executable creates a PNG that is named like the script itself
        Process process = rProcessor.execute(command);

        // Capture process output
        String processOutput = IOUtils.toString(process.getInputStream(), String.valueOf(StandardCharsets.UTF_8));
        String processError = IOUtils.toString(process.getErrorStream(), String.valueOf(StandardCharsets.UTF_8));

        // Log process messages
        log.info("Process Output:\n{}", processOutput);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            // Log process errors
            log.error("Process Errors:\n{}", processError);
            throw new RuntimeException("Failed to execute modified R script. Exit code: " + exitCode);
        }
        Files.deleteIfExists(modifiedScriptPath);
    }


}