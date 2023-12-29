package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(PersistWordController.class)
class PersistWordControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Service s3Service;

    @Test
    void should_upload_to_s3() throws Exception {
        // Given
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.docx", "application/octet-stream", "Test content".getBytes());

        // When
        when(s3Service.uploadFileToS3(any(MockMultipartFile.class))).thenReturn("test.docx");

        // Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors/s3")
                        .file(mockMultipartFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Word document uploaded to S3 with key: test.docx"));
    }

    @Test
    void should_download_document() throws Exception {
        // Given
        String fileName = "test.docx";
        byte[] content = "Test content".getBytes();
        when(s3Service.downloadFileFromS3(fileName)).thenReturn(content);

        // Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/{fileName}", fileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"test.docx\""))
                .andExpect(MockMvcResultMatchers.content().bytes(content));
    }

    @Test
    void should_list_documents() throws Exception {
        // Given
        List<String> documentList = List.of("doc1", "doc2");
        when(s3Service.listFilesOfBucket()).thenReturn(documentList);

        // Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(documentList.size()));
    }
}
