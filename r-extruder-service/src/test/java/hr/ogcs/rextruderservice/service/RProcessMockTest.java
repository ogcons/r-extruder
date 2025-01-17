package hr.ogcs.rextruderservice.service;

import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RProcessMockTest {

    private RProcessMock rProcessMock;

    @BeforeEach
    void setUp() {
        rProcessMock = new RProcessMock();
    }

    @Test
    void should_create_dummy_png_from_dummy_rfile() throws IOException, InterruptedException, DocumentException {
        // Given
        var command = "";
        var outputfilename = "r-script.png";
        var destination = ".";

        // When
        var process = rProcessMock.execute(command,outputfilename, Path.of(destination), false);

        // Then
        assertNotNull(process);
        assertEquals(0, process.waitFor());
        var expectedPngFileName = "r-script.png";
        var targetPngFile = new File("." + File.separator + expectedPngFileName);

        assertTrue(targetPngFile.exists(), "PNG file should exist");

        Files.deleteIfExists(targetPngFile.toPath());
    }

    @Test
    void shoudl_create_dummy_pdf_with_images_from_dummy_rfile() throws DocumentException, IOException, InterruptedException {
        // Given
        var command = "";
        var outputFileName = "output.pdf";
        boolean generatePdfWithPictures = true;
        var destination = ".";

        // When
        var process = rProcessMock.execute(command, outputFileName, Path.of(destination), generatePdfWithPictures);

        // Then
        File pdfFile = new File(destination, outputFileName);
        assertTrue(pdfFile.exists(), "PDF file should be created");
        assertEquals(0, process.waitFor());
        for (int i = 0; i < 3; i++) {
            File imageFile = new File(destination, outputFileName.replace(".pdf", "") + "_" + i + ".png");
            assertTrue(imageFile.exists(), "Image file should be created");
            Files.deleteIfExists(imageFile.toPath());
        }
        Files.deleteIfExists(Path.of(outputFileName));
    }
}