package hr.ogcs.rextruderservice.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfConvertServiceTest {
    private PdfConvertService pdfConvertService;

    @BeforeEach
    void setUp() {
        pdfConvertService = new PdfConvertService();
    }

    @Test
    void should_convert_pdf_to_valid_word_document() throws Exception {
        // Given
        Path pdfPath = Paths.get("src/test/resources/testfile.pdf");
        byte[] pdfByte = Files.readAllBytes(pdfPath);
        List<byte[]> pdfBytes = Arrays.asList(pdfByte, pdfByte);
        MultipartFile mpf = new MockMultipartFile("test", new FileInputStream("src/test/resources/testfile.pdf"));
        List<MultipartFile> mpfs = Arrays.asList(mpf, mpf);

        // When
        byte[] result = pdfConvertService.convertPdfToWord(pdfBytes, mpfs);

        // Then
        assertNotNull(result, "The result should not be null");
        try (InputStream inputStream = new ByteArrayInputStream(result)) {
            XWPFDocument generated = new XWPFDocument(inputStream);
            assertFalse(generated.getParagraphs().isEmpty(), "The document should contain paragraphs");

            PDDocument pdfDocument = PDDocument.load(pdfByte);
            assertEquals(pdfDocument.getNumberOfPages(), generated.getAllPictures().size(),
                    "The number of pictures should match the number of pages in the PDF file");
            generated.getAllPictures().forEach(picture -> assertEquals("png", picture.suggestFileExtension(), "The picture should be a PNG"));
        }

        // Clean up
        Path imagePath = Paths.get("1.png");
        Files.deleteIfExists(imagePath);
    }

    @Test
    void should_test_process_pdf_correctly_and_insert_images_in_word() throws Exception {
        // Given
        Path pdfPath = Paths.get("src/test/resources/testfile.pdf");
        byte[] pdfByte = Files.readAllBytes(pdfPath);
        MultipartFile mpf = new MockMultipartFile("test", new FileInputStream("src/test/resources/testfile.pdf"));
        XWPFDocument document = new XWPFDocument();

        // When
        pdfConvertService.processPdf(pdfByte, document, mpf);

        PDDocument pdfDocument = PDDocument.load(pdfByte);

        // Then
        assertEquals(pdfDocument.getNumberOfPages(), document.getAllPictures().size(), "The number of pictures should match the number of pages in the PDF file");

        // Clean up
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            Path imagePath = Paths.get(i + ".png");
            Files.deleteIfExists(imagePath);
        }
    }

    @Test
    void should_save_image() throws IOException {
        // Given
        BufferedImage image = ImageIO.read(new File("src/test/resources/testfile.png"));

        // When
        String result = pdfConvertService.saveImage(image, 1, "testfile");

        // Then
        File file = new File(result);
        assertTrue(file.exists());

        // Clean up
        Files.deleteIfExists(file.toPath());
    }

    @Test
    void should_insert_image_in_word() throws IOException, InvalidFormatException {
        // Given
        XWPFDocument document = new XWPFDocument();

        // When
        pdfConvertService.insertImageToWord(document, "src/test/resources/testfile.png");

        // Then
        assertEquals(1, document.getAllPictures().size(), "The document should contain one picture");
        assertEquals("png", document.getAllPictures().get(0).suggestFileExtension(), "The picture should be a PNG");
    }
}
