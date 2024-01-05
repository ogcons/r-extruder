package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.RScriptService;
import hr.ogcs.rextruderservice.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@WebMvcTest(RExtruderController.class)
class RExtruderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RScriptService rScriptService;

    @MockBean
    private S3Service s3Service;

    @Test
    void createAndUpload() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.docx", "application/octet-stream", "Test content".getBytes());
        when(rScriptService.createPlotFromRScript(any())).thenReturn("Dummy Word Bytes".getBytes());
        when(s3Service.uploadFileToS3(any(byte[].class), any(String.class))).thenReturn("dummy_key.docx");

        // When
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Word document uploaded to S3 with key:dummy_key.docx"));
    }

    @Test
    void downloadDocument() throws Exception {
        // Given
        String fileName = "dummy_key.docx";
        byte[] documentBytes = "Dummy Word Bytes".getBytes();
        when(s3Service.downloadFileFromS3(fileName)).thenReturn(documentBytes);

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"dummy_key.docx\""))
                .andExpect(MockMvcResultMatchers.content().bytes(documentBytes));
    }

    @Test
    void listDocuments() throws Exception {
        // Given
        when(s3Service.listFilesOfBucket()).thenReturn(Arrays.asList("file1.docx", "file2.docx"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("file1.docx"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value("file2.docx"));
    }
}