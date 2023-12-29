package hr.ogcs.rextruderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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
        var command = "some-r-file.r";

        // When
        var process = rProcessMock.execute(command);

        // Then
        assertNotNull(process);
        assertEquals(0, process.waitFor());
        var expectedPngFileName = "some-r-file.r.png";
        var targetPngFile = new File("." + File.separator + expectedPngFileName);

        assertTrue(targetPngFile.exists(), "PNG file should exist");
    }
}