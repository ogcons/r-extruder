package hr.ogcs.rextruderservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Profile("mock")
@Component
public class RProcessMock implements RProcessor {
    @Value("${rscript.uploadDir}")
    private String uploadDir;

    @Override
    public Process execute(String command) throws IOException {

        var rFile = command.split(" ")[command.split(" ").length-1];
        //TODO Set target filename folder to the same folder like the real implementation
        var targetFilenamePng = uploadDir + File.separator + rFile + ".png";

        // Creates blank image
        BufferedImage img = new BufferedImage(256, 256,
                BufferedImage.TYPE_INT_RGB);
        File f = new File(targetFilenamePng);
        ImageIO.write(img, "PNG", f);

        // returning dummy process
        // nslookup is one of the commands that works on Windows and Unix machines
        return new ProcessBuilder("nslookup", "localhost").start();

    }
}