package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
@Slf4j
public class RScriptService {

    private final DocumentService documentService;

    private final RProcessor rProcessor;

    @Value("${rscript.path}")
    private String rScriptPath;

    @Value("${rscript.workingDir}")
    private String workingDir = ".";

    public RScriptService(DocumentService documentService, RProcessor rProcessor) {
        this.documentService = documentService;
        this.rProcessor = rProcessor;
    }

    public byte[] createPlotFromRScript(MultipartFile uploadedFile) throws IOException, InterruptedException {
        try {
            Path scriptFilePath = saveRScript(uploadedFile);
            byte[] plotBytes = executeRScriptAndRetrievePlot(scriptFilePath);
            return documentService.generateWord(plotBytes);
        } catch (IOException | InterruptedException e) {
            log.error("IOException during R script execution: {}", e.getMessage());
            throw e;
        }
    }

    protected Path saveRScript(MultipartFile uploadedFile) throws IOException {
        String originalFilename = uploadedFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Original filename is null or empty.");
        }
        if (!originalFilename.toUpperCase().endsWith(".R")) {
            throw new IllegalArgumentException("Invalid file type. Only .R files are allowed.");
        }
        Path uploadDirPath = Paths.get(workingDir);
        Path filePath = uploadDirPath.resolve(originalFilename);
        uploadedFile.transferTo(filePath.toFile());
        return filePath;
    }

    protected byte[] executeRScriptAndRetrievePlot(Path scriptFilePath) throws IOException, InterruptedException {
        Path outputFilePath = scriptFilePath.resolveSibling(
                scriptFilePath.getFileName().toString().replace(".R", ".png"));

        String modifiedScriptContent = modifyScriptContent(scriptFilePath, outputFilePath.getFileName().toString());
        Path modifiedScriptPath = saveModifiedScript(modifiedScriptContent, scriptFilePath.getFileName().toString());
        try {
            executeRScript(modifiedScriptPath, outputFilePath);

            Files.move(outputFilePath, scriptFilePath.resolveSibling(outputFilePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);

            return Files.readAllBytes(scriptFilePath.resolveSibling(outputFilePath.getFileName()));
        } catch (IOException | InterruptedException e) {
            log.error("Error during R script execution: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extends the original R script by code that enables the plotting of PNG files.
     * @param scriptFilePath R script file path
     * @param outputFileName R script file name
     * @return modified R script content
     * @throws IOException Can happen if there is no R script
     */
    protected String modifyScriptContent(Path scriptFilePath, String outputFileName) throws IOException {
        String scriptContent = Files.readString(scriptFilePath);

        outputFileName = (outputFileName != null) ? outputFileName : "default_output";

        // Modify script to save the plot as a PNG
        if (scriptContent.contains("plot(")) {
            scriptContent = scriptContent.replace("plot(", String.format("png('%s')%splot(", outputFileName, System.lineSeparator()));
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
        Path modifiedScriptPath = Paths.get(workingDir, "modified_" + scriptFileName.replace(" ", "_"));
        Files.write(modifiedScriptPath, modifiedScriptContent.getBytes(), StandardOpenOption.CREATE);
        return modifiedScriptPath;
    }

    protected void executeRScript(Path modifiedScriptPath, Path outputFilePath) throws IOException, InterruptedException {
        if (modifiedScriptPath == null || modifiedScriptPath.getFileName() == null) {
            // Handle the null case, throw an exception, or log an error
            throw new IllegalArgumentException("Invalid modifiedScriptPath");
        }
        String command = rScriptPath + " " + modifiedScriptPath.toAbsolutePath();

        // The external R executable creates a PNG that is named like the script itself
        Process process = rProcessor.execute(command, outputFilePath.getFileName().toString(), outputFilePath.getParent());

        String processOutput = IOUtils.toString(process.getInputStream(), String.valueOf(StandardCharsets.UTF_8));
        String processError = IOUtils.toString(process.getErrorStream(), String.valueOf(StandardCharsets.UTF_8));

        log.info("Process Output:\n{}", processOutput);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Log process errors
            log.error("Process Errors:\n{}", processError);
            throw new IOException("Failed to execute modified R script. Exit code: " + exitCode);
        }
    }
}