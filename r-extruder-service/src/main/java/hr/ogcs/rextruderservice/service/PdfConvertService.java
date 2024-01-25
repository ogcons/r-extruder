package hr.ogcs.rextruderservice.service;

import hr.ogcs.rextruderservice.model.RPlotsData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

@Service
public class PdfConvertService {

    @Value("${word.document.image.width}")
    private int imageWidth;

    @Value("${word.document.image.height}")
    private int imageHeight;

    @Value("${rscript.workingDir}")
    private String workingDir = ".";

    public byte[] convertPdfToWord(List<RPlotsData> rMetaDataList) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            int size = rMetaDataList.size();
            for (int i = 0; i < size; i++) {
                RPlotsData rMetaData = rMetaDataList.get(i);
                processPdf(rMetaData, document, i == size - 1);
            }

            // Save the Word document to a ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    protected void processPdf(RPlotsData rMetaData, XWPFDocument document, boolean isLastImage) throws IOException {        try (PDDocument pdfDocument = PDDocument.load(rMetaData.getPlotFile())) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);

        for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String imagePath;
                imagePath = saveImage(image, page + 1, rMetaData.getFileName());

                insertImageToWord(document, imagePath, rMetaData.getFileName(),
                        page == pdfDocument.getNumberOfPages() - 1 && isLastImage);
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

    protected void insertImageToWord(XWPFDocument document, String imagePath, String filename, boolean isLastImage) throws IOException, InvalidFormatException {
        XWPFParagraph filenameParagraph = document.createParagraph();
        filenameParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun filenameRun = filenameParagraph.createRun();
        filenameRun.setText(filename);

        XWPFParagraph imageParagraph = document.createParagraph();
        imageParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun imageRun = imageParagraph.createRun();

        try (InputStream imageStream = new FileInputStream(imagePath)) {
            imageRun.addPicture(imageStream, Document.PICTURE_TYPE_PNG, "Generated Image", Units.toEMU(imageWidth), Units.toEMU(imageHeight));
        }

        if (!isLastImage) {
            imageRun.addBreak(BreakType.PAGE);
        }
    }
}
