package hr.ogcs.rextruderservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("mock")
class RScriptServiceComponentTest {

    @Autowired
    private RScriptService rScriptService;

    @Test
    void should_generate_word_document_from_dummy_input() throws IOException, InterruptedException {
        // Given
        String filename= "testfile.R";
        boolean generatePdf = false;
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getOriginalFilename()).thenReturn(filename);
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getBytes()).thenReturn("R script content".getBytes());

        MultipartFile[] mockMultipartFiles = {mockMultipartFile};

        // When
        byte[] result = rScriptService.createPlotFromRScripts(mockMultipartFiles, generatePdf);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Clean up
        String modified = "modified_" + filename.replace(" ", "_");
        Path modifiedScriptPath = Path.of("src", "test", "resources", modified);
        Files.deleteIfExists(modifiedScriptPath);
    }
}