package hr.ogcs.rextruderservice.service;

import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.nio.file.Path;

public interface RProcessor {
    Process execute(String command, String outputFileName, Path destinationPath, boolean generatePdfWithPictures) throws IOException, DocumentException;
}