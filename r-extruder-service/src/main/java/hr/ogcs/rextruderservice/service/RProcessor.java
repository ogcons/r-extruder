package hr.ogcs.rextruderservice.service;

import java.io.IOException;
import java.nio.file.Path;

public interface RProcessor {
    Process execute(String command, String outputFileName, Path destinationPath) throws IOException;
}