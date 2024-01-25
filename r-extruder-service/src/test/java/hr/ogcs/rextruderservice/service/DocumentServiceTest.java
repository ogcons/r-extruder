package hr.ogcs.rextruderservice.service;

import hr.ogcs.rextruderservice.model.RPlotsData;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        var plotAsByte = Objects.requireNonNull(classLoader.getResource("testfile.png")).getFile().getBytes();
        String originalFilename = "filename.R";


        var plotsData = new RPlotsData(plotAsByte,originalFilename);
        List<RPlotsData> rMetaDataList = new ArrayList<>();
        rMetaDataList.add(plotsData);

        // When
        var result = documentService.generateCombinedWord(rMetaDataList);

        // Then : Checks if the result is a valid Word document, containing exactly one png image
        assertNotNull(result);
        try (InputStream inputStream = new ByteArrayInputStream(result)) {
            XWPFDocument generated = new XWPFDocument(inputStream);
            assertFalse(generated.getParagraphs().isEmpty());
            assertEquals(1, generated.getAllPictures().size());
            assertEquals("png", generated.getAllPictures().get(0).suggestFileExtension());
            assertEquals(originalFilename, generated.getParagraphs().get(0).getParagraphText());
        }
    }

    @Test
    void should_not_put_page_break_at_the_end_of_word() throws IOException {
        // Given
        ClassLoader classLoader = getClass().getClassLoader();
        var plotAsByte = Objects.requireNonNull(classLoader.getResource("testfile.png")).getFile().getBytes();
        RPlotsData rPlot1 = new RPlotsData(plotAsByte, "anyString");
        RPlotsData rPlot2 = new RPlotsData(plotAsByte, "anyString");

        // When
        byte[] result = documentService.generateCombinedWord(Arrays.asList(rPlot1, rPlot2));

        // Then
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(result))) {

            // Can't count pages, but can paragraphs. 4 from Plot data and 1 from page break.
            assertEquals(5, document.getParagraphs().size());
        }
    }

    @Test
    void should_throw_exception_when_image_is_empty() {
        // Given
        ArrayList<RPlotsData> rMetaDataList = new ArrayList<>();

        // When and Then
        assertThrows(IllegalArgumentException.class, () -> documentService.generateCombinedWord(rMetaDataList));
    }
}