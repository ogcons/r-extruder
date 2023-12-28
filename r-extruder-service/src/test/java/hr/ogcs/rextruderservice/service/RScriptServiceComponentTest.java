package hr.ogcs.rextruderservice.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Profile("mock")
class RScriptServiceComponentTest {

    private RScriptService rScriptService;

    @Mock
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        rScriptService = new RScriptService();
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
    }

}
