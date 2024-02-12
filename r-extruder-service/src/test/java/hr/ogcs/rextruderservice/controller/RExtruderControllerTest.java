package hr.ogcs.rextruderservice.controller;

import hr.ogcs.rextruderservice.service.RScriptService;
import hr.ogcs.rextruderservice.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
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
    void should_create_and_upload_in_s3_bucket() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.R", "application/octet-stream", "Test content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.R", "application/octet-stream", "Test content 2".getBytes());

        when(rScriptService.createPlotFromRScripts(any(), eq(false)))
                .thenReturn("Dummy Word Bytes".getBytes());
        when(s3Service.uploadFileToS3(any(byte[].class), any(String.class))).thenReturn("dummy_key.docx");

        // When
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors")
                        .file(file1)
                        .file(file2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string("{\"s3 key\":\"dummy_key.docx\",\"message\":\"Word document uploaded to S3 with key: dummy_key.docx\"}"));
    }

    @Test
    void should_create_and_upload_in_s3_bucket_with_docx_output() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("files", "test.R", "application/octet-stream", "Test content".getBytes());
        when(rScriptService.createPlotFromRScripts(any(), eq(false))).thenReturn("Dummy Word Bytes".getBytes());
        when(s3Service.uploadFileToS3(any(byte[].class), any(String.class))).thenReturn("dummy_key.docx");

        // When
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors")
                        .file(file)
                        .param("output", "docx"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"dummy_key.docx\""))
                .andExpect(MockMvcResultMatchers.content().bytes("Dummy Word Bytes".getBytes()));
    }

    @Test
    void should_handle_exception_during_create_and_upload() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("files", "test.docx", "application/octet-stream", "Test content".getBytes());

        when(rScriptService.createPlotFromRScripts(any(),eq(false))).thenThrow(new IOException("IO Exception"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("File " + file.getOriginalFilename() + " has an unsupported extension. Only files with .R extension are allowed."));
    }

    @Test
    void should_handle_io_exception_during_create_and_upload() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("files", "test.R", "application/octet-stream", "Test content".getBytes());

        when(rScriptService.createPlotFromRScripts(any(), eq(false))).thenThrow(new IOException("IO Exception"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Failed to perform the combined operation"));
    }

    @Test
    void should_download_document_from_s3_bucket() throws Exception {
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
    void should_list_documents_from_s3_bucket() throws Exception {
        // Given
        when(s3Service.listFilesOfBucket()).thenReturn(Arrays.asList("file1.docx", "file2.docx"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("file1.docx"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value("file2.docx"));
    }

    @Test
    void should_handle_internal_server_error_during_upload() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.docx", "application/octet-stream", "Test content".getBytes());
        when(rScriptService.createPlotFromRScripts(any(), eq(false))).thenThrow(new IOException("Simulated error"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/extractors")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    void should_handle_internal_server_error_during_download() throws Exception {
        // Given
        String fileName = "dummy_key.docx";
        when(s3Service.downloadFileFromS3(fileName)).thenThrow(new IOException("Simulated error"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/{fileName}", fileName))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void should_handle_internal_server_error_during_listing() throws Exception {
        // Given
        when(s3Service.listFilesOfBucket()).thenThrow(new IOException("Simulated error"));

        // When
        mockMvc.perform(MockMvcRequestBuilders.get("/api/extractors/s3/"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}