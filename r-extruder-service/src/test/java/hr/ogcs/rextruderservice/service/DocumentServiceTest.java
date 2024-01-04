package hr.ogcs.rextruderservice.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService();
    }

    @Test
    void should_generate_word_from_given_plot_image() throws IOException {

        // Given
        ClassLoader classLoader = getClass().getClassLoader();
        var plotAsByte = classLoader.getResource("testfile.png").getFile().getBytes();

        // When
        var result = documentService.generateWord(plotAsByte);

        // Then : Checks if the result is a valid Word document, containing exactly one png image
        assertNotNull(result);
        try (InputStream inputStream = new ByteArrayInputStream(result)) {
            XWPFDocument generated = new XWPFDocument(inputStream);
            assertFalse(generated.getParagraphs().isEmpty());
            assertEquals(1, generated.getAllPictures().size());
            assertEquals("png", generated.getAllPictures().get(0).suggestFileExtension());
        }
    }

    @Test
    void should_throw_exception_when_image_is_empty() {
        // When and Then
        assertThrows(IllegalArgumentException.class, () -> documentService.generateWord(new byte[0]));
    }


}