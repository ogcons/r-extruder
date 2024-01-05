package hr.ogcs.rextruderservice.service;

import hr.ogcs.rextruderservice.service.DocumentService;
import hr.ogcs.rextruderservice.service.RProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("mock")
class RScriptServiceComponentTest {

    @Autowired
    private RScriptService rScriptService;

    @Test
    void should_generate_word_document_from_dummy_input() throws IOException, InterruptedException {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("testfile.R");
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getBytes()).thenReturn("R script content".getBytes());

        byte[] result = rScriptService.createPlotFromRScript(mockMultipartFile);

    }
}
