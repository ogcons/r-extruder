package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class DocumentService {

    @Value("${word.document.image.width}")
    private int imageWidth;

    @Value("${word.document.image.height}")
    private int imageHeight;

    public byte[] generateWord(byte[] plotBytes) throws IOException {
        if (plotBytes.length == 0 ) {
            throw new IllegalArgumentException("Image is empty!");
        }
        try {
            // Create Word document
            XWPFDocument document = new XWPFDocument();

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();

            // Add the PNG to the paragraph
            int imageFormat = Document.PICTURE_TYPE_PNG;
            String imageId = document.addPictureData(new ByteArrayInputStream(plotBytes), imageFormat);

            XWPFPicture picture = run.addPicture(new ByteArrayInputStream(plotBytes),
                    imageFormat, "image.png",
                    Units.toEMU(imageWidth), Units.toEMU(imageHeight));
            picture.getCTPicture().getBlipFill().getBlip().setEmbed(imageId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);

            return baos.toByteArray();
        } catch (Exception e) {
            // Log the exception for troubleshooting purposes
            log.error("Error during Word document generation: {}", e.getMessage());

            // Rethrow the exception as IOException
            throw new IOException("Error during Word document generation", e);
        }
    }

}
