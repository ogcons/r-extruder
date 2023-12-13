package hr.ogcs.rextruderservice.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import static hr.ogcs.rextruderservice.service.RScriptService.UPLOAD_DIR;
import static org.junit.jupiter.api.Assertions.*;

class RScriptServiceTest {

    private RScriptService rScriptService;


    @BeforeEach
    void setUp() {
        rScriptService = new RScriptService();
        cleanUpTempFiles();

    }

    @Test
    void should_upload_and_execute_rscript(@TempDir Path tempDir) throws IOException, InterruptedException, InvalidFormatException {
        // Given
        String originalFileName = "test_script.R";
        Path tempFilePath = tempDir.resolve(originalFileName);
        Files.write(tempFilePath, "plot(c(1,2,3))".getBytes());
        MockMultipartFile multipartFile = new MockMultipartFile("file", originalFileName, "text/plain", Files.readAllBytes(tempFilePath));

        // When
        byte[] result = rScriptService.uploadAndExecuteRScript(multipartFile);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        saveByteArrayToFile(result, "generatedWordDocument.docx");
    }


    @Test
    void should_upload_rscript(@TempDir Path tempDir) throws IOException {
        // Given
        String originalFileName = "test_script.R";
        Path tempFilePath = tempDir.resolve(originalFileName);
        Files.write(tempFilePath, "Test script content".getBytes());
        MockMultipartFile multipartFile = new MockMultipartFile("file", originalFileName, "text/plain", Files.readAllBytes(tempFilePath));

        // When
        String resultFileName = rScriptService.uploadRScript(multipartFile);

        // Then
        assertNotNull(resultFileName);
        assertTrue(Files.exists(Paths.get(UPLOAD_DIR, resultFileName)));
    }

    @Test
    void should_execute_rscript_and_retrieve_plot(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Given
        String testScriptFileName = "test_script.R";
        Files.write(Paths.get(UPLOAD_DIR, testScriptFileName), "plot(c(1,2,3))".getBytes());

        // When
        byte[] plotBytes = rScriptService.executeRScriptAndRetrievePlot(testScriptFileName);

        // Then
        assertNotNull(plotBytes);
        assertTrue(plotBytes.length > 0);
    }

    @Test
    void should_modify_script_content() throws IOException {
        // Given
        String testScriptFileName = "test_script.R";
        String testScriptContent = "plot(c(1,2,3))";
        Files.write(Paths.get(UPLOAD_DIR, testScriptFileName), testScriptContent.getBytes());

        // When
        String modifiedScriptContent = rScriptService.modifyScriptContent(testScriptFileName, "output.png");

        // Then
        assertTrue(modifiedScriptContent.contains("png('output.png')\nplot(c(1,2,3))\ndev.off()"));
    }

    @Test
    void should_save_modified_script() throws IOException {
        // Given
        String modifiedScriptContent = "modified content";

        // When
        Path modifiedScriptPath = rScriptService.saveModifiedScript(modifiedScriptContent);

        // Then
        assertNotNull(modifiedScriptPath);
        assertTrue(Files.exists(modifiedScriptPath));
    }

    @Test
     void should_execute_rscript() throws IOException, InterruptedException {
        // Given
        Path tempDir = Files.createTempDirectory("temp_scripts");

        String rScriptContent = "# Create some sample data\n" +
                "x <- 1:10\n" +
                "y <- c(2, 4, 6, 8, 10, 8, 6, 4, 2, 0)\n" +
                "\n" +
                "# Create a basic plot\n" +
                "plot(x, y, type = \"l\", col = \"blue\", lwd = 2, main = \"Simple Plot\", xlab = \"X-axis\", ylab = \"Y-axis\")";

        Path modifiedScriptPath = Files.createTempFile(tempDir, "script", ".R");

        try {
            Files.write(modifiedScriptPath, rScriptContent.getBytes());

            // When
            rScriptService.executeRScript(modifiedScriptPath);

            // Then
            assertFalse(Files.exists(modifiedScriptPath), "Temporary file should be deleted after execution");
        } finally {
            // Delete the temporary directory and its content
            Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
    @Test
    void should_generate_word() throws IOException, InvalidFormatException, InterruptedException, InvalidFormatException {
        // Given
        MockMultipartFile uploadedFile = new MockMultipartFile("script.R", "script.R", "text/plain", "plot(1:10)".getBytes());
        rScriptService.uploadAndExecuteRScript(uploadedFile);

        // When
        byte[] result = rScriptService.generateWord();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        saveByteArrayToFile(result, "generatedWordDocument.docx");
    }

    private void saveByteArrayToFile(byte[] byteArray, String fileName) throws IOException {
        Path filePath = Paths.get(RScriptService.UPLOAD_DIR, fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(byteArray);
        }
    }
    @Test
    void should_get_rscript_content(@TempDir Path tempDir) throws IOException {
        // Given
        String originalFileName = "test_script.R";
        Path tempFilePath = tempDir.resolve(originalFileName);
        String scriptContent = "plot(c(1,2,3))";
        Files.write(tempFilePath, scriptContent.getBytes());

        // When
        MockMultipartFile multipartFile = new MockMultipartFile("file", originalFileName, "text/plain", Files.readAllBytes(tempFilePath));
        rScriptService.uploadRScript(multipartFile);

        String generatedFileName = rScriptService.getAllRScriptNames().get(0);
        byte[] retrievedContent = rScriptService.getRScriptContent(generatedFileName);

        // Then
        assertNotNull(retrievedContent);
        assertArrayEquals(scriptContent.getBytes(), retrievedContent);
    }

    @Test
    void should_get_all_rscript_names(@TempDir Path tempDir) throws Exception {
        // Given
        String scriptFileName1 = "test_script1.R";
        Path tempFilePath1 = tempDir.resolve(scriptFileName1);
        Files.write(tempFilePath1, "plot(c(1,2,3))".getBytes());

        // When
        String scriptFileName2 = "test_script2.R";
        Path tempFilePath2 = tempDir.resolve(scriptFileName2);
        Files.write(tempFilePath2, "plot(c(4,5,6))".getBytes());

        // When
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", scriptFileName1, "text/plain", Files.readAllBytes(tempFilePath1));
        MockMultipartFile multipartFile2 = new MockMultipartFile("file", scriptFileName2, "text/plain", Files.readAllBytes(tempFilePath2));
        rScriptService.uploadRScript(multipartFile1);
        rScriptService.uploadRScript(multipartFile2);

        List<String> scriptNames = rScriptService.getAllRScriptNames();


        // Remove atomic counter prefix from script names
        List<String> sanitizedScriptNames = scriptNames.stream()
                .map(name -> name.replaceFirst("^\\d+_",""))
                .collect(Collectors.toList());

        // Then
        assertNotNull(sanitizedScriptNames);
        assertEquals(2, sanitizedScriptNames.size());
        assertTrue(sanitizedScriptNames.contains(scriptFileName1));
        assertTrue(sanitizedScriptNames.contains(scriptFileName2));
    }
    private void cleanUpTempFiles() {
        try {
            // Walk through the UPLOAD_DIR and delete only .R and .png files created during testing
            Files.walk(Paths.get(UPLOAD_DIR))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.endsWith(".R") || fileName.endsWith(".png");
                    })
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
