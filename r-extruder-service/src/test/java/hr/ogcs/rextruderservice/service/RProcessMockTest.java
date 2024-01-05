package hr.ogcs.rextruderservice.service;

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
    void should_create_dummy_png_from_dummy_rfile() throws IOException, InterruptedException {

        // Given
        var command = "";
        var outputfilename = "r-script.png";
        var destination = ".";

        // When
        var process = rProcessMock.execute(command,outputfilename, Path.of(destination));

        // Then
        assertNotNull(process);
        assertEquals(0, process.waitFor());
        var expectedPngFileName = "r-script.png";
        var targetPngFile = new File("." + File.separator + expectedPngFileName);

        assertTrue(targetPngFile.exists(), "PNG file should exist");

        Files.deleteIfExists(targetPngFile.toPath());
    }
}