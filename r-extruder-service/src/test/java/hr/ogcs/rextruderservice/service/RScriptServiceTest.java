package hr.ogcs.rextruderservice.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.*;

class RScriptServiceTest {

    private RScriptService rScriptService;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        rScriptService = new RScriptService();

        setFieldValue(rScriptService, "rScriptPath", "C:\\Program Files\\R\\R-4.3.2\\bin\\Rscript.exe");
    }
    private void setFieldValue(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
    void should_throw_exception_on_failed_execution(@TempDir Path tempDir) throws IOException {
        // Given
        String originalFileName = "test_script.R";
        Path tempFilePath = tempDir.resolve(originalFileName);
        Files.write(tempFilePath, "invalid_r_code".getBytes());
        MockMultipartFile multipartFile = new MockMultipartFile("file", originalFileName, "text/plain", Files.readAllBytes(tempFilePath));

        // When and Then
        assertThrows(RuntimeException.class, () -> rScriptService.uploadAndExecuteRScript(multipartFile));
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
        assertTrue(Files.exists(Paths.get(System.getenv("UPLOAD_DIR"), resultFileName)));
    }

    @Test
    void should_execute_rscript_and_retrieve_plot() throws IOException, InterruptedException {
        // Given
        String testScriptFileName = "test_script.R";
        Files.write(Paths.get(System.getenv("UPLOAD_DIR"), testScriptFileName), "plot(c(1,2,3))".getBytes());

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
        Files.write(Paths.get(System.getenv("UPLOAD_DIR"), testScriptFileName), testScriptContent.getBytes());

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

        String rScriptContent = "x <- 1:10\n" + "y <- c(2, 4, 6, 8, 10, 8, 6, 4, 2, 0)\n";

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



    private void saveByteArrayToFile(byte[] byteArray, String fileName) throws IOException {
        Path filePath = Paths.get(System.getenv("UPLOAD_DIR"), fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(byteArray);
        }
    }

}
