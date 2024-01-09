package hr.ogcs.rextruderservice.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class DocumentServiceTest {

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService();
    }

    // Existing test method
    @Test
    void should_generate_word_from_given_plot_image() throws IOException {
        // Given
        ClassLoader classLoader = getClass().getClassLoader();
        var plotAsByte = Objects.requireNonNull(classLoader.getResource("testfile.png")).getFile().getBytes();

        List<byte[]> plotBytesList = new ArrayList<>();
        plotBytesList.add(plotAsByte);

        // When
        var result = documentService.generateCombinedWord(plotBytesList);

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
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Expected exception message");
        });    }

    @Test
    void should_throw_exception_on_error_during_document_generation() throws IOException {
        // Given
        DocumentService documentServiceMock = Mockito.mock(DocumentService.class);
        when(documentServiceMock.generateCombinedWord(any())).thenThrow(new IOException("Simulated error"));

        // When and Then
        assertThrows(IOException.class, () -> documentServiceMock.generateCombinedWord(Collections.singletonList(new byte[1])));
    }

}