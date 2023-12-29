package hr.ogcs.rextruderservice.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Profile("mock")
@Component
public class RProcessMock implements RProcessor {
    @Override
    public Process execute(String command) throws IOException {
        try {
            var RFile = command.split(" ")[0];
            var folderPath = "src/main/resources/files";
            var targetFilenamePng = folderPath + File.separator + RFile + ".png";

            BufferedImage img = new BufferedImage(256, 256,
                    BufferedImage.TYPE_INT_RGB);
            File f = new File(targetFilenamePng);
            ImageIO.write(img, "PNG", f);

            return new ProcessBuilder("cd").start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}