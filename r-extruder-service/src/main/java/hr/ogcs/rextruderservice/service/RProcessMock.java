package hr.ogcs.rextruderservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Profile("mock")
@Component
public class RProcessMock implements RProcessor {

    @Value("${rscript.uploadDir}")
    private String uploadDir;

    @Override
    public Process execute(String command, String outputFileName, Path destinationPath) throws IOException {
        BufferedImage dummyImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

        File outputImageFile = new File(destinationPath.toFile(), outputFileName);
        ImageIO.write(dummyImage, "png", outputImageFile);

        // Returning a dummy process
        // nslookup is one of the commands that works on Windows and Unix machines
        return new ProcessBuilder("nslookup", "localhost").start();
    }
}