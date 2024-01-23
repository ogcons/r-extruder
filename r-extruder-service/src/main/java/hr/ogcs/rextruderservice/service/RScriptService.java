package hr.ogcs.rextruderservice.service;

import hr.ogcs.rextruderservice.exception.RScriptProcessingException;

import hr.ogcs.rextruderservice.model.RPlotsData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RScriptService {

    private final DocumentService documentService;

    private final RProcessor rProcessor;

    private final PdfConvertService pdfConvertService;

    @Value("${rscript.path}")
    private String rScriptPath;

    @Value("${rscript.workingDir}")
    private String workingDir = ".";


    public RScriptService(DocumentService documentService, RProcessor rProcessor, PdfConvertService pdfConvertService) {
        this.documentService = documentService;
        this.rProcessor = rProcessor;
        this.pdfConvertService = pdfConvertService;
    }

    public byte[] createPlotFromRScripts(MultipartFile[] uploadedFiles, boolean generatePdfWithPictures) throws IOException {
        List<RPlotsData> allPlots = Arrays.stream(uploadedFiles).map(uploadedFile -> {
            try {

                Path scriptFilePath = saveRScript(uploadedFile);
                byte[] plotbytes;
                if (generatePdfWithPictures) {
                    plotbytes = executeRScriptAndGeneratePdf(scriptFilePath);
                } else {
                    plotbytes = executeRScriptAndRetrievePlot(scriptFilePath);
                }
                return new RPlotsData(plotbytes, uploadedFile.getOriginalFilename());
            } catch (IOException | InterruptedException e) {
                log.error("Error during creation of plot for file {}: {}", uploadedFile.getOriginalFilename(), e.getMessage());
                throw new RScriptProcessingException(e);
            }
        }).toList();

        // Convert all plots into a Word document
        if (generatePdfWithPictures) {
            return pdfConvertService.convertPdfToWord(allPlots);
        } else {
            return documentService.generateCombinedWord(allPlots);
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

    protected byte[] executeRScriptAndGeneratePdf(Path scriptFilePath) throws IOException, InterruptedException {
        try {
            Path outputFilePath = scriptFilePath.resolveSibling(
                    scriptFilePath.getFileName().toString().replace(".R", ".pdf"));

            String modifiedScriptContent = modifyScriptContentForPdfOnly(scriptFilePath, outputFilePath.getFileName().toString());
            Path modifiedScriptPath = saveModifiedScript(modifiedScriptContent, scriptFilePath.getFileName().toString());

            executeRScript(modifiedScriptPath, outputFilePath);
            Files.move(outputFilePath, scriptFilePath.resolveSibling(outputFilePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);

            return Files.readAllBytes(outputFilePath);
        } catch (IOException | InterruptedException e) {
            log.error("Error during R script execution and PDF conversion: {}", e.getMessage());
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
        String scriptContent = Files.readString(scriptFilePath, StandardCharsets.UTF_8).trim();

        outputFileName = (outputFileName != null) ? outputFileName : "default_output";

        // Modify script: add png() to save plot as a picture
        if (scriptContent.contains("plot(")) {
            scriptContent = scriptContent.replace("plot(", String.format("png('%s')%nplot(", outputFileName));
            scriptContent += "\ndev.off()";
        }

        // Modify script: add pdf() png(), set plotfit to FALSE and add dev.off()
        if (scriptContent.replace(" ", "").contains("Fit<-try(KinEval(")) {
            int fitIndex = scriptContent.indexOf("Fit    <- try(KinEval(");
            int endOfFit = scriptContent.indexOf(")", fitIndex) + 1;
            String fitSection = scriptContent.substring(fitIndex, endOfFit);

            fitSection = String.format("pdf('%s')%n", outputFileName.replace(".png", ".pdf")) +
                    String.format("png('%s')%n", outputFileName) +
                    fitSection;

            if (scriptContent.replace(" ", "").contains("plotfit=TRUE")) {
                fitSection = fitSection.replace("plotfit   = TRUE", "plotfit   = FALSE");
            }

            int kinReportIndex = scriptContent.indexOf("KinReport(", endOfFit);
            String devOffCommand = "dev.off()" + System.lineSeparator();
            scriptContent = scriptContent.substring(0, kinReportIndex) + devOffCommand + scriptContent.substring(kinReportIndex);

            scriptContent = scriptContent.substring(0, fitIndex) + fitSection + scriptContent.substring(endOfFit);
        }
        return scriptContent;
    }
    protected String modifyScriptContentForPdfOnly(Path scriptFilePath, String outputFileName) throws IOException {
        String scriptContent = Files.readString(scriptFilePath);
        outputFileName = (outputFileName != null) ? outputFileName : "default_output";

        // If pdf is wanted. Modify script: add pdf()
        if (scriptContent.replace(" ", "").contains("Fit<-try(KinEval(")) {
            int fitIndex = scriptContent.indexOf("Fit    <- try(KinEval(");
            int endOfFit = scriptContent.indexOf(")", fitIndex) + 1;
            String fitSection = scriptContent.substring(fitIndex, endOfFit);

            fitSection = String.format("pdf(\"%s\")%n", outputFileName.replace(".png", ".pdf")) +
                    fitSection;
            if (scriptContent.replace(" ", "").contains("plotfit=TRUE")) {
                fitSection = fitSection.replace("plotfit   = TRUE", "plotfit   = FALSE");
            }

            scriptContent = scriptContent.substring(0, fitIndex) + fitSection + scriptContent.substring(endOfFit);
        }
        return scriptContent;
    }

    protected Path saveModifiedScript(String modifiedScriptContent, String scriptFileName) throws IOException {
        Path modifiedScriptPath = Paths.get(workingDir, "modified_" + scriptFileName.replace(" ", "_"));
        Files.write(modifiedScriptPath, modifiedScriptContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
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