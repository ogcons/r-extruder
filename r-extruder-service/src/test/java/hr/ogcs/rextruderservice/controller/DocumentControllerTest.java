package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.S3Service;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private DocumentController s3Controller;

    @Test
    void should_upload_to_s3() throws IOException, InterruptedException, InvalidFormatException {
        // Given
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.docx", "application/octet-stream", "Test content".getBytes());

        // When
        when(s3Service.uploadFileToS3(any(MockMultipartFile.class))).thenReturn("test.docx");
        ResponseEntity<String> response = s3Controller.uploadToS3(mockMultipartFile);

        // Then
        assertEquals("Word document uploaded to S3 with key: test.docx", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void should_download_document() throws IOException {
        // Given
        String fileName = "test.docx";
        byte[] content = "Test content".getBytes();
        when(s3Service.downloadFileFromS3(fileName)).thenReturn(content);

        // When
        ResponseEntity<Resource> response = s3Controller.downloadDocument(fileName);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ByteArrayResource resource = (ByteArrayResource) response.getBody();
        assert resource != null;
        assertArrayEquals(content, resource.getByteArray());
    }

    @Test
    void should_list_documents() throws IOException {
        // Given
        List<String> documentList = List.of("doc1", "doc2");

        //When
        when(s3Service.listFilesOfBucket()).thenReturn(documentList);
        ResponseEntity<List<String>> response = s3Controller.listDocuments();

        // Then
        assertEquals(documentList, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void should_handle_upload_exception() throws IOException, InterruptedException, InvalidFormatException {
        // Given
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.docx", "application/octet-stream", "Test content".getBytes());

        // When
        doThrow(IOException.class).when(s3Service).uploadFileToS3(any(MultipartFile.class));
        ResponseEntity<String> response = s3Controller.uploadToS3(mockMultipartFile);

        // Then
        assertEquals("Failed to upload Word document to S3", response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void should_handle_download_exception() throws IOException {
        // Given
        String fileName = "test.docx";

        // When
        doThrow(IOException.class).when(s3Service).downloadFileFromS3(fileName);
        ResponseEntity<Resource> response = s3Controller.downloadDocument(fileName);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void should_handle_list_exception() throws IOException {
        // When
        doThrow(IOException.class).when(s3Service).listFilesOfBucket();
        ResponseEntity<List<String>> response = s3Controller.listDocuments();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}