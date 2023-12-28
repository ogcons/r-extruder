package hr.ogcs.rextruderservice.service;

import org.springframework.context.annotation.Profile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// TODO try without this anno: @Service
@Profile("mock")
public class RProcessMock implements RProcessor {
    @Override
    public Process execute(String command) {
        try {
            var RFile = command.split(" ")[0];
            // TODO create PNG file at expected location/folder
            var targetFilenamePng = RFile + ".png";
            BufferedImage img = new BufferedImage(256, 256,
                    BufferedImage.TYPE_INT_RGB);
            File f = new File(targetFilenamePng);
            ImageIO.write(img, "PNG", f);

            return new ProcessBuilder("dir").start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
