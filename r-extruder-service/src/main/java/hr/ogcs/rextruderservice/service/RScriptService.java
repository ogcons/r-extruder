package hr.ogcs.rextruderservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class RScriptService {
    String UPLOAD_DIR = new File(getClass().getClassLoader().getResource("files").getFile()).getAbsolutePath();
    private final AtomicLong counter = new AtomicLong(0);

    @Getter
    public String outputFileName;

    public byte[] uploadAndExecuteRScript(MultipartFile uploadedFile) throws IOException, InterruptedException, InvalidFormatException {
        String scriptFileName = uploadRScript(uploadedFile);

        executeRScriptAndRetrievePlot(scriptFileName);

        return generateWord();
    }

    String uploadRScript(MultipartFile uploadedFile) throws IOException {
        String fileName = counter.getAndIncrement() + "_" + uploadedFile.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        uploadedFile.transferTo(filePath.toFile());

        return fileName;
    }

    byte[] executeRScriptAndRetrievePlot(String scriptFileName) throws IOException, InterruptedException {
        outputFileName = scriptFileName.replace(".R", ".png");

        // Modify script to save the plot as a PNG
        String modifiedScriptContent = modifyScriptContent(scriptFileName, outputFileName);

        // Save modified script as a temp file
        Path modifiedScriptPath = saveModifiedScript(modifiedScriptContent);

        // Execute script
        executeRScript(modifiedScriptPath);

        // Move plot to the same dir as script
        Path destinationPath = Paths.get(UPLOAD_DIR, outputFileName);
        Files.move(Paths.get(outputFileName), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return Files.readAllBytes(destinationPath);
    }

    String modifyScriptContent(String scriptFileName, String outputFileName) throws IOException {
        String scriptContent = new String(Files.readAllBytes(Paths.get(UPLOAD_DIR, scriptFileName)), StandardCharsets.UTF_8);

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
        String rScriptPath = "C:\\Program Files\\R\\R-4.3.2\\bin\\Rscript.exe";

        String command = rScriptPath + " " + modifiedScriptPath.toAbsolutePath().toString();

        Process process = Runtime.getRuntime().exec(command);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to execute modified R script. Exit code: " + exitCode);
        }

        Files.deleteIfExists(modifiedScriptPath);
    }
    public byte[] generateWord() throws IOException, InvalidFormatException {
        if (outputFileName == null) {
            throw new IllegalStateException("Output file name is not set");
        }

        File imageFile = new File(UPLOAD_DIR, outputFileName);
        InputStream imageInputStream = new FileInputStream(imageFile);

        XWPFDocument document = new XWPFDocument();

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        // Add the PNG to the paragraph
        int imageFormat = XWPFDocument.PICTURE_TYPE_PNG;
        String imageId = document.addPictureData(imageInputStream, imageFormat);
        int width = 360;
        int height = 360;

        XWPFPicture picture = run.addPicture(imageInputStream, imageFormat, "image.png", Units.toEMU(width), Units.toEMU(height));
        picture.getCTPicture().getBlipFill().getBlip().setEmbed(imageId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);

        return baos.toByteArray();
    }

    public byte[] getRScriptContent(String fileName) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        return Files.readAllBytes(filePath);
    }

    public List<String> getAllRScriptNames() {
        try (Stream<Path> walk = Files.walk(Paths.get(UPLOAD_DIR))) {
            // Filter only files with ".R" extension
            return walk.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".R"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error getting all R script names", e);
            return Collections.emptyList();
        }
    }

}