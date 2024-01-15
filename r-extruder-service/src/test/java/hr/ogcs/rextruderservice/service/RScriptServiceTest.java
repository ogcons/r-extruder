package hr.ogcs.rextruderservice.service;

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

    @InjectMocks
    private RScriptService rScriptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rScriptService = new RScriptService(documentService, rProcessor);
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
    void should_execute_rscript_with_valid_paths() throws IOException, InterruptedException {
        // Given
        Path modifiedScriptPath = Files.createTempFile("modified", ".R");
        Path outputFilePath = Files.createTempFile("output", ".png");

        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);

        // Process process = rProcessor.execute(command, outputFilePath.getFileName().toString(), outputFilePath.getParent());
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));

        //When
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);

        // Then
        assertDoesNotThrow(() -> rScriptService.executeRScript(modifiedScriptPath, outputFilePath));

        verify(rProcessor, times(1)).execute(any(), any(), any());
        verify(mockedProcess, times(1)).getInputStream();
        verify(mockedProcess, times(1)).getErrorStream();
        verify(mockedProcess, times(1)).waitFor();

        // Clean up
        Files.deleteIfExists(modifiedScriptPath);
        Files.deleteIfExists(outputFilePath);
    }

    @Test
    void should_throw_exception_for_process_fail() throws IOException, InterruptedException {
        // Given
        Path modifiedScriptPath = Files.createTempFile("modified", ".R");
        Path outputFilePath = Files.createTempFile("output", ".png");

        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);

        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));

        // When
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(1);

        // Then
        assertThrows(IOException.class, () -> rScriptService.executeRScript(modifiedScriptPath, outputFilePath));

        verify(rProcessor, times(1)).execute(any(), any(), any());
        verify(mockedProcess, times(1)).getInputStream();
        verify(mockedProcess, times(1)).getErrorStream();
        verify(mockedProcess, times(1)).waitFor();

        // Clean up
        Files.deleteIfExists(modifiedScriptPath);
        Files.deleteIfExists(outputFilePath);
    }

    @Test
    void should_throw_exception_for_invalid_path_in_execute_rscript() throws IOException {
        // Given
        Path outputFilePath = Paths.get(".");

        // When and Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rScriptService.executeRScript(null, outputFilePath));

        assertEquals("Invalid modifiedScriptPath", exception.getMessage());
        verify(rProcessor, never()).execute(any(), any(), any());
    }

    @Test
    void should_execute_rscript_and_retrieve_plot() throws IOException, InterruptedException {
        // Given
        Path resourceFile = Paths.get("src", "test", "resources", "testfile.R");

        //When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);
        when(documentService.generateCombinedWord(argThat((List<byte[]> list) -> true)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        byte[] result = rScriptService.executeRScriptAndRetrievePlot(resourceFile);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Clean up
        Files.deleteIfExists(Path.of("." + File.separator + "modified_testfile.R"));
    }

    @Test
    void should_throw_exception_for_failed_execution_of_retrieve_plot() throws IOException, InterruptedException {
        // Given
        Path resourceFile = Paths.get("src", "test", "resources", "testfile.R");

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);

        when(mockedProcess.waitFor()).thenThrow(new InterruptedException("Simulated interruption"));

        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(documentService.generateCombinedWord(argThat((List<byte[]> list) -> true)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // Then
        assertThrows(InterruptedException.class, () -> rScriptService.executeRScriptAndRetrievePlot(resourceFile));

        // Clean up
        Files.deleteIfExists(Paths.get(".", "modified_testfile.R"));
    }

    @Test
    void should_throw_exception_for_invalid_file_in_execute_and_retrieve_plot() throws IOException {
        // Given
        Path invalidFilePath = Paths.get("non_existent_file.R");

        // When and Then
        IOException exception = assertThrows(IOException.class,
                () -> rScriptService.executeRScriptAndRetrievePlot(invalidFilePath));

        assertEquals("java.nio.file.NoSuchFileException", exception.getClass().getName());
        assertTrue(exception.getMessage().contains("non_existent_file.R"));

        verify(rProcessor, never()).execute(any(), any(), any());
        verify(documentService, never()).generateCombinedWord(argThat((List<byte[]> list) -> true));
    }

    @Test
    void should_create_plot_from_rscript() throws IOException, InterruptedException {
        // Given
        RScriptService rScriptService = new RScriptService(documentService, rProcessor);

        String originalFileName = "test_script.R";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", "plot(c(1,2,3))".getBytes());
        String modifiedFileName = "modified_" + originalFileName.replace(" ", "_");

        String pngFilePath = "." + File.separator + "test_script.png";
        Path path = Path.of(pngFilePath);
        Files.writeString(path, "PNG Content", StandardOpenOption.CREATE);

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);

        when(documentService.generateCombinedWord(anyList())).thenAnswer(invocation -> {
            List<byte[]> byteArrays = invocation.getArgument(0);
            return byteArrays.get(0);
        });

        byte[] result = rScriptService.createPlotFromRScripts(new MultipartFile[]{multipartFile});

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
    void should_throw_exception_for_failed_creating_plot() throws IOException, InterruptedException {
        // Given
        String originalFileName = "test_script.R";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", "plot(c(1,2,3))".getBytes());
        String modifiedFileName = "modified_" + originalFileName.replace(" ", "_");

        String pngFilePath = "." + File.separator + "test_script.png";
        Path path = Path.of(pngFilePath);
        Files.writeString(path, "PNG Content", StandardOpenOption.CREATE);

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);
        when(mockedProcess.waitFor()).thenReturn(1);
        when(mockedProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8)));
        when(mockedProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8)));
        when(documentService.generateCombinedWord(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        IOException exception = assertThrows(IOException.class,
                () -> rScriptService.createPlotFromRScripts(new MockMultipartFile[]{multipartFile}),
                "Expected createPlotFromRScript to throw IOException");

        assertTrue(exception.getMessage().contains("Failed to execute modified R script. Exit code: 1"));

        // Clean up
        Files.deleteIfExists(path);
        Files.deleteIfExists(Path.of(originalFileName));
        Files.deleteIfExists(Path.of(modifiedFileName));
    }
}
