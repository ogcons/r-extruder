package hr.ogcs.rextruderservice.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Test
    void should_upload_word_to_s3() throws IOException, InterruptedException, InvalidFormatException {
        // Given
        byte [] content = "Test content".getBytes();
        String originalFileName = "filename";

        // When
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
        String objectKey = s3Service.uploadFileToS3(content, originalFileName);
        String actualFileName = objectKey.substring(0, objectKey.lastIndexOf('_'));

        // Then
        assertEquals(originalFileName, actualFileName);
    }

    @Test
    void should_download_word_from_s3() throws IOException {
        // Given
        String objectKey = "test.docx";
        byte[] content = "Test content".getBytes();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(getObjectResponse, content);

        // When
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        byte[] result = s3Service.downloadFileFromS3(objectKey);

        // Then
        assertArrayEquals(content, result);
    }

    @Test
    void should_list_documents_in_bucket() throws IOException {
        // Given
        ListObjectsV2Response listObjectsResponse = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("doc1").build(), S3Object.builder().key("doc2").build())
                .build();

        // When
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listObjectsResponse);
        List<String> documentList = s3Service.listFilesOfBucket();

        // Then
        assertEquals(List.of("doc1", "doc2"), documentList);
    }

   @Test
    void should_handle_upload_exceptions() throws IOException, InterruptedException, InvalidFormatException {
        // Given
        byte [] content = "Test content".getBytes();
        String originalFileName = "filename";
        // When
        doThrow(S3Exception.class).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Then
        assertThrows(IOException.class, () -> s3Service.uploadFileToS3(content,originalFileName));
    }

    @Test
    void should_handle_download_exceptions(){
        // Given
        String objectKey = "test.docx";

        // When
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(S3Exception.class);

        // Then
        assertThrows(IOException.class, () -> s3Service.downloadFileFromS3(objectKey));
    }

    @Test
    void should_handle_list_exceptions() {
        // When
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(S3Exception.class);

        // Then
        assertThrows(IOException.class, () -> s3Service.listFilesOfBucket());
    }
}
