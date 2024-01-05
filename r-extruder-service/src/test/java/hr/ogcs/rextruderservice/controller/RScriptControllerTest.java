package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.RScriptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class RScriptControllerTest {

    @InjectMocks
    private RScriptController rScriptController;

    @Mock
    private RScriptService rScriptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_upload_and_download() throws IOException, InterruptedException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "testScript.R", MediaType.TEXT_PLAIN_VALUE, "Test script content".getBytes());
        byte[] expectedWordBytes = "Generated Word Document".getBytes();

        // When
        when(rScriptService.createPlotFromRScript(mockFile)).thenReturn(expectedWordBytes);
        ResponseEntity<byte[]> responseEntity = rScriptController.uploadAndDownload(mockFile);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, responseEntity.getHeaders().getContentType());
        assertEquals("form-data; name=\"attachment\"; filename=\"generatedDocument.docx\"", Objects.requireNonNull(responseEntity.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).get(0));
        assertEquals(expectedWordBytes, responseEntity.getBody());
    }

    @Test
    void should_throw_error_for_upload_and_download() throws IOException, InterruptedException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "testScript.R", MediaType.TEXT_PLAIN_VALUE, "Test script content".getBytes());

        // When
        when(rScriptService.createPlotFromRScript(mockFile)).thenThrow(new IOException("Error executing script"));

        // Then
        assertThrows(IOException.class, () -> {
            rScriptController.uploadAndDownload(mockFile);
        });
    }

}
