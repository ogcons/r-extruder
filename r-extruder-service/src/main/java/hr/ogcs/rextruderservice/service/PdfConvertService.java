package hr.ogcs.rextruderservice.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Objects;

@Service
public class PdfConvertService {

    @Value("${rscript.workingDir}")
    private String workingDir = ".";

    public byte[] convertPdfToWord(List<byte[]> pdfBytes, List<MultipartFile> mpf) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            int i = 0;
            for (byte[] pdfByte : pdfBytes) {
                processPdf(pdfByte, document, mpf.get(i++));
            }

            // Save the Word document to a ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    protected void processPdf(byte[] pdfBytes, XWPFDocument document, MultipartFile mpsf) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(pdfBytes)) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);

            for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String imagePath;
                imagePath = saveImage(image, page + 1, Objects.requireNonNull(mpsf.getOriginalFilename()));

                insertImageToWord(document, imagePath);
            }
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    protected String saveImage(BufferedImage image, int pageNumber, String originalFilename) throws IOException {
        String filename = workingDir + File.separator + originalFilename.replace(".R", "_") + pageNumber + ".png";
        ImageIO.write(image, "png", new File(filename));
        return filename;
    }

    protected void insertImageToWord(XWPFDocument document, String imagePath) throws IOException, InvalidFormatException {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        try (InputStream imageStream = new FileInputStream(imagePath)) {
            run.addPicture(imageStream, Document.PICTURE_TYPE_PNG, "Generated Image", Units.toEMU(300), Units.toEMU(300));
        }
    }
}