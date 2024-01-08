package hr.ogcs.rextruderservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("testfile.R");
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getBytes()).thenReturn("R script content".getBytes());

        // When
        byte[] result = rScriptService.createPlotFromRScript(mockMultipartFile);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}