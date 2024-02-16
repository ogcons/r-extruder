package hr.ogcs.rextruderservice.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
/**
 * Creates a dummy image for the document processing.
 * The image is normally created by R script and this behavior is mocked here.
 */
@Profile("mock")
@Service
public class RProcessMock implements RProcessor {

    @Override
    public Process execute(String command, String outputFileName, Path destinationPath, boolean generatePdfWithPictures) throws IOException, DocumentException {
        if (generatePdfWithPictures) {
            // Create 3 dummy images
            for (int i = 0; i < 3; i++) {
                BufferedImage dummyImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                File outputImageFile = new File(destinationPath.toFile(), outputFileName.replace(
                        ".pdf","") + "_" + i + ".png");
                ImageIO.write(dummyImage, "png", outputImageFile);
            }

            // Create a PDF and insert the images
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(destinationPath.toFile() + File.separator + outputFileName));
            document.open();
            for (int i = 0; i < 3; i++) {
                Image image = Image.getInstance(destinationPath.toFile() + File.separator + outputFileName.replace(
                        ".pdf", "") + "_" + i + ".png");
                document.add(image);
            }
            document.close();
        } else {
            BufferedImage dummyImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            File outputImageFile = new File(destinationPath.toFile(), outputFileName);
            ImageIO.write(dummyImage, "png", outputImageFile);
        }

        // Returning a dummy process
        // nslookup is one of the commands that works on Windows and Unix machines
        return new ProcessBuilder("nslookup", "localhost").start();
    }
}