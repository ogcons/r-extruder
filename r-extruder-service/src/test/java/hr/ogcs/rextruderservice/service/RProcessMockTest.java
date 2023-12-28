package hr.ogcs.rextruderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RProcessMockTest {

    private RProcessMock rProcessMock;

    @BeforeEach
    void setUp() {
        rProcessMock = new RProcessMock();
    }

    @Test
    void execute() {
        var command = "ls some-r-file.r";
        var execute = rProcessMock.execute(command);

        assertNotNull(execute);
        // TODO assert that PNG exists
    }
}