package hr.ogcs.rextruderservice.service;

import com.itextpdf.text.DocumentException;
import hr.ogcs.rextruderservice.exception.RScriptProcessingException;
import hr.ogcs.rextruderservice.model.RPlotsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RScriptServiceTest {

    @Mock
    private RProcessor rProcessor;
    @Mock
    private DocumentService documentService;

    @Mock PdfConvertService pdfConvertService;

    @InjectMocks
    private RScriptService rScriptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rScriptService = new RScriptService(documentService, rProcessor, pdfConvertService);
    }

    @Test
    void should_save_rscript_and_return_valid_path() throws IOException {
        // Given
        String workingDir = ".";
        String originalFilename = "testfile.R";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                "text/plain",
                "Hello, World!".getBytes()
        );

        // When
        Path resultPath = rScriptService.saveRScript(mockMultipartFile);

        // Then
        assertNotNull(resultPath);
        assertEquals(Path.of(workingDir, originalFilename), resultPath);

        Files.deleteIfExists(resultPath);
    }

    @Test
    void should_throw_exception_for_null_or_empty_filename_in_save_script() {
        // Given
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                null,
                "text/plain",
                "Hello, World!".getBytes()
        );

        // When and Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> rScriptService.saveRScript(mockMultipartFile));
        assertEquals("Original filename is null or empty.", exception.getMessage());
    }

    @Test
    void should_throw_exception_for_invalid_file_in_save_script() {
        // Given
        String originalFilename = "file.txt";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                "text/plain",
                "Hello, World!".getBytes()
        );

        // When and Then
        assertThrows(IllegalArgumentException.class, () -> rScriptService.saveRScript(mockMultipartFile));
    }

    @Test
    void should_modify_rscript_content() throws IOException {
        // Given
        String scriptContent = "plot(data); Fit    <- try(KinEval(plotfit   = TRUE)) KinReport();";
        String outputFileName = "output.png";

        Path tempScriptFile = Files.createTempFile("testScript", ".R");
        Files.write(tempScriptFile, scriptContent.getBytes());

        // When
        String modifiedContent = rScriptService.modifyScriptContent(tempScriptFile, outputFileName);

        // Then
        assertTrue(modifiedContent.contains(String.format("png('%s')", outputFileName)));
        assertTrue(modifiedContent.contains("dev.off()"));

        Files.deleteIfExists(tempScriptFile);
    }

    @Test
    void should_modify_rscript_content_for_pdf_only() throws IOException {
        // Given
        String scriptContent = "Fit    <- try(KinEval(plotfit   = TRUE)) KinReport();";
        String outputFileName = "output.p";

        Path tempScriptFile = Files.createTempFile("testScript", ".R");
        Files.write(tempScriptFile, scriptContent.getBytes());

        // When
        String modifiedContent = rScriptService.modifyScriptContent(tempScriptFile, outputFileName);

        // Then
        assertTrue(modifiedContent.contains(String.format("pdf('%s')", outputFileName)));
        assertTrue(modifiedContent.replace(" ", "").contains("plotfit=FALSE"));

        Files.deleteIfExists(tempScriptFile);
    }

    @Test
    void should_save_modified_rscript() throws IOException {
        // Given
        String modifiedScriptContent = "modified script content";
        String scriptFileName = "test_script.R";

        // When
        Path modifiedScriptPath = rScriptService.saveModifiedScript(modifiedScriptContent, scriptFileName);

        // Then
        assertTrue(Files.exists(modifiedScriptPath));
        assertTrue(Files.isRegularFile(modifiedScriptPath));
        assertEquals("." + File.separator + "modified_test_script.R", modifiedScriptPath.toString());

        // Clean up
        Files.deleteIfExists(modifiedScriptPath);
    }

    @Test
    void should_execute_rscript_with_valid_paths() throws IOException, InterruptedException, DocumentException {
        // Given
        Path modifiedScriptPath = Files.createTempFile("modified", ".R");
        Path outputFilePath = Files.createTempFile("output", ".png");

        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(),eq(false))).thenReturn(mockedProcess);

        // Process process = rProcessor.execute(command, outputFilePath.getFileName().toString(), outputFilePath.getParent());
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));

        //When
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);

        // Then
        assertDoesNotThrow(() -> rScriptService.executeRScript(modifiedScriptPath, outputFilePath, false));

        verify(rProcessor, times(1)).execute(any(), any(), any(), eq(false));
        verify(mockedProcess, times(1)).getInputStream();
        verify(mockedProcess, times(1)).getErrorStream();
        verify(mockedProcess, times(1)).waitFor();

        // Clean up
        Files.deleteIfExists(modifiedScriptPath);
        Files.deleteIfExists(outputFilePath);
    }

    @Test
    void should_throw_exception_for_process_fail() throws IOException, InterruptedException, DocumentException {
        // Given
        Path modifiedScriptPath = Files.createTempFile("modified", ".R");
        Path outputFilePath = Files.createTempFile("output", ".png");

        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(),eq(false))).thenReturn(mockedProcess);

        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));

        // When
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(1);

        // Then
        assertThrows(IOException.class, () -> rScriptService.executeRScript(modifiedScriptPath, outputFilePath, false));

        verify(rProcessor, times(1)).execute(any(), any(), any(), eq(false));
        verify(mockedProcess, times(1)).getInputStream();
        verify(mockedProcess, times(1)).getErrorStream();
        verify(mockedProcess, times(1)).waitFor();

        // Clean up
        Files.deleteIfExists(modifiedScriptPath);
        Files.deleteIfExists(outputFilePath);
    }

    @Test
    void should_throw_exception_for_invalid_path_in_execute_rscript() throws IOException, DocumentException {
        // Given
        Path outputFilePath = Paths.get(".");

        // When and Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rScriptService.executeRScript(null, outputFilePath, false));

        assertEquals("Invalid modifiedScriptPath", exception.getMessage());
        verify(rProcessor, never()).execute(any(), any(), any(), eq(false));
    }

    @Test
    void should_execute_rscript_and_retrieve_plot() throws IOException, InterruptedException, DocumentException {
        // Given
        Path resourceFile = Paths.get("src", "test", "resources", "testfile.R");

        //When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), eq(false))).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);
        when(documentService.generateCombinedWord(argThat((List<RPlotsData> list) -> true)))
                .thenAnswer(invocation -> {
                    List<RPlotsData> list = invocation.getArgument(0);
                    return list.get(0).getPlotFile();
                });
        byte[] result = rScriptService.executeRScriptAndRetrievePlot(resourceFile);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Clean up
        Files.deleteIfExists(Path.of("." + File.separator + "modified_testfile.R"));
    }

    @Test
    void should_throw_exception_for_failed_execution_of_retrieve_plot() throws IOException, InterruptedException, DocumentException {
        // Given
        Path resourceFile = Paths.get("src", "test", "resources", "testfile.R");

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), eq(false))).thenReturn(mockedProcess);

        when(mockedProcess.waitFor()).thenThrow(new InterruptedException("Simulated interruption"));

        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(documentService.generateCombinedWord(argThat((List<RPlotsData> list) -> true)))
                .thenAnswer(invocation -> {
                    List<RPlotsData> list = invocation.getArgument(0);
                    return list.get(0).getPlotFile();
                });
        // Then
        assertThrows(InterruptedException.class, () -> rScriptService.executeRScriptAndRetrievePlot(resourceFile));

        // Clean up
        Files.deleteIfExists(Paths.get(".", "modified_testfile.R"));
    }

    @Test
    void should_throw_exception_for_invalid_file_in_execute_and_retrieve_plot() throws IOException, DocumentException {
        // Given
        Path invalidFilePath = Paths.get("non_existent_file.R");

        // When and Then
        IOException exception = assertThrows(IOException.class,
                () -> rScriptService.executeRScriptAndRetrievePlot(invalidFilePath));

        assertEquals("java.nio.file.NoSuchFileException", exception.getClass().getName());
        assertTrue(exception.getMessage().contains("non_existent_file.R"));

        verify(rProcessor, never()).execute(any(), any(), any(), anyBoolean());
        verify(documentService, never()).generateCombinedWord(argThat((List<RPlotsData> list) -> true));    }

    @Test
    void should_execute_rscript_and_generate_pdf() throws IOException, InterruptedException, DocumentException {
        // Given
        Path resourceFile = Paths.get("src", "test", "resources", "testfile.R");

        //When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), eq(false))).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);
        when(documentService.generateCombinedWord(argThat((List<RPlotsData> list) -> true)))
                .thenAnswer(invocation -> {
                    List<RPlotsData> list = invocation.getArgument(0);
                    return list.get(0).getPlotFile();
                });
        byte[] result = rScriptService.executeRScriptAndGeneratePdf(resourceFile, false);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Clean up
        Files.deleteIfExists(Path.of("." + File.separator + "modified_testfile.R"));
    }

    @Test
    void should_throw_exception_for_failed_execution_of_retrieve_pdf() throws IOException, InterruptedException, DocumentException {
        // Given
        Path resourceFile = Paths.get("src", "test", "resources", "testfile.R");

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), eq(false))).thenReturn(mockedProcess);

        when(mockedProcess.waitFor()).thenThrow(new InterruptedException("Simulated interruption"));

        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(documentService.generateCombinedWord(argThat((List<RPlotsData> list) -> true)))
                .thenAnswer(invocation -> {
                    List<RPlotsData> list = invocation.getArgument(0);
                    return list.get(0).getPlotFile();
                });

        // Then
        assertThrows(InterruptedException.class, () -> rScriptService.executeRScriptAndGeneratePdf(resourceFile,false));


        // Clean up
        Files.deleteIfExists(Path.of("." + File.separator + "modified_testfile.R"));
    }

    @Test
    void should_create_plot_from_rscript() throws Exception {
        // Given
        RScriptService rScriptService = new RScriptService(documentService, rProcessor, pdfConvertService);

        boolean generatePdf = false;
        String originalFileName = "test_script.R";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", "plot(c(1,2,3))".getBytes());
        String modifiedFileName = "modified_" + originalFileName.replace(" ", "_");

        String pngFilePath = "." + File.separator + "test_script.png";
        Path path = Path.of(pngFilePath);
        Files.writeString(path, "PNG Content", StandardOpenOption.CREATE);

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), anyBoolean())).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);

        when(documentService.generateCombinedWord(anyList())).thenAnswer(invocation -> {
            List<RPlotsData> rPlotsDataList = invocation.getArgument(0);
            return rPlotsDataList.get(0).getPlotFile();
        });

        byte[] result = rScriptService.createPlotFromRScripts(new MultipartFile[]{multipartFile}, generatePdf);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify that the generated plot is equal to the content of test_script.png
        byte[] expectedContent = Files.readAllBytes(path);
        assertArrayEquals(expectedContent, result);

        // Clean up
        Files.deleteIfExists(path);
        Files.deleteIfExists(Path.of(originalFileName));
        Files.deleteIfExists(Path.of(modifiedFileName));
    }

    @Test
    void should_throw_exception_for_failed_creating_plot() throws IOException, InterruptedException, DocumentException {
        // Given
        String originalFileName = "test_script.R";
        boolean generatePdf = false;
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", "plot(c(1,2,3))".getBytes());
        String modifiedFileName = "modified_" + originalFileName.replace(" ", "_");

        String pngFilePath = "." + File.separator + "test_script.png";
        Path path = Path.of(pngFilePath);
        Files.writeString(path, "PNG Content", StandardOpenOption.CREATE);

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), anyBoolean())).thenReturn(mockedProcess);
        when(mockedProcess.waitFor()).thenReturn(1);
        when(mockedProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8)));
        when(mockedProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8)));
        when(documentService.generateCombinedWord(anyList())).thenAnswer(invocation -> {
            List<RPlotsData> rPlotsDataList = invocation.getArgument(0);
            return rPlotsDataList.get(0).getPlotFile();
        });

        // Then
        RScriptProcessingException exception = assertThrows(RScriptProcessingException.class, () -> rScriptService.createPlotFromRScripts(new MultipartFile[]{multipartFile}, generatePdf));

        assertTrue(exception.getMessage().contains("Failed to execute modified R script. Exit code: 1"));
        verify(rProcessor, times(1)).execute(any(), any(), any(),anyBoolean());
        verify(mockedProcess, times(1)).waitFor();
        verify(mockedProcess, times(1)).getInputStream();
        verify(mockedProcess, times(1)).getErrorStream();
        verify(documentService, times(0)).generateCombinedWord(anyList());

        // Clean up
        Files.deleteIfExists(path);
        Files.deleteIfExists(Path.of(originalFileName));
        Files.deleteIfExists(Path.of(modifiedFileName));
    }

    @Test
    void should_create_plots_from_rscript_using_generated_pdf() throws Exception {
        // Given
        RScriptService rScriptService = new RScriptService(documentService, rProcessor, pdfConvertService);

        boolean generatePdf = true;
        String originalFileName = "test_script.R";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", "Fit    <- try(KinEval(plotfit   = TRUE)) KinReport();".getBytes());
        String modifiedFileName = "modified_" + originalFileName.replace(" ", "_");

        String pdfFilePath = "." + File.separator + "test_script.pdf";
        Path path = Path.of(pdfFilePath);
        Files.writeString(path, "PDF Content", StandardOpenOption.CREATE);

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any(), anyBoolean())).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);

        when(pdfConvertService.convertPdfToWord(anyList())).thenAnswer(invocation -> {
            List<RPlotsData> rPlotsDataList = invocation.getArgument(0);
            return rPlotsDataList.get(0).getPlotFile();
        });

        byte[] result = rScriptService.createPlotFromRScripts(new MultipartFile[]{multipartFile}, generatePdf);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify that the generated plot is equal to the content of test_script.png
        byte[] expectedContent = Files.readAllBytes(path);
        assertArrayEquals(expectedContent, result);

        // Clean up
        Files.deleteIfExists(path);
        Files.deleteIfExists(Path.of(originalFileName));
        Files.deleteIfExists(Path.of(modifiedFileName));
    }
}