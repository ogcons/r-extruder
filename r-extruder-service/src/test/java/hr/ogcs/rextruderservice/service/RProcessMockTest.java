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
    void execute() throws IOException {
        var command = "cd some-r-file.r";
        var execute = rProcessMock.execute(command);

        assertNotNull(execute);

        var expectedPngFileName = "some-r-file.r.png";
        var folderPath = "src/main/resources/files";
        var targetPngFile = new File(folderPath + File.separator + expectedPngFileName);

        assertTrue(targetPngFile.exists(), "PNG file should exist");
    }
}