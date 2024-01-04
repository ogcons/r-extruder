package hr.ogcs.rextruderservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
        String uploadDir = ".";
        String originalFilename = "testfile.R";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                "text/plain",
                "Hello, World!".getBytes()
        );

        Path resultPath = rScriptService.saveRScript(mockMultipartFile);

        assertNotNull(resultPath);
        assertEquals(Path.of(uploadDir, originalFilename), resultPath);

        Files.deleteIfExists(resultPath);
    }
    @Test
    void should_throw_exception_for_invalid_file_type() {
        String originalFilename = "file.txt";
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                "text/plain",
                "Hello, World!".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            rScriptService.saveRScript(mockMultipartFile);
        });
    }

    @Test
    void should_modify_rscript_content() throws IOException {
        String scriptContent = "plot(data); KinReport(results);";
        String outputFileName = "output.png";

        Path tempScriptFile = Files.createTempFile("testScript", ".R");
        Files.write(tempScriptFile, scriptContent.getBytes());

        String modifiedContent = rScriptService.modifyScriptContent(tempScriptFile, outputFileName);

        assertTrue(modifiedContent.contains(String.format("png('%s')", outputFileName)));
        assertTrue(modifiedContent.contains("dev.off()"));

        Files.deleteIfExists(tempScriptFile);
    }

    @Test
    void should_save_modified_rscript() throws IOException {
        // Mocking values
        String modifiedScriptContent = "modified script content";
        String scriptFileName = "test_script.R";

        // Calling the method to test
        Path modifiedScriptPath = rScriptService.saveModifiedScript(modifiedScriptContent, scriptFileName);

        // Verifying the result
        assertTrue(Files.exists(modifiedScriptPath));
        assertTrue(Files.isRegularFile(modifiedScriptPath));
        assertEquals("." + File.separator + "modified_test_script.R", modifiedScriptPath.toString());

        // Cleaning up
        Files.deleteIfExists(modifiedScriptPath);
    }

    @Test
    void should_execute_rscript_with_valid_paths() throws IOException, InterruptedException {
        // Given
        Path modifiedScriptPath = Files.createTempFile("modified",".R");
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
    void should_throw_exception_when_invalid_path() throws IOException {
        // Given
        Path modifiedScriptPath = null;
        Path outputFilePath = Paths.get(".");

        // When and Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> rScriptService.executeRScript(modifiedScriptPath, outputFilePath));

        assertEquals("Invalid modifiedScriptPath", exception.getMessage());
        verify(rProcessor, never()).execute(any(), any(), any());
    }

    @Test
    void should_execute_rscript_and_retrieve_plot() throws IOException, InterruptedException {

        // Given
        Path resourceFile = Paths.get("src","test","resources", "testfile.R");

        //When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);
        when(documentService.generateWord(any(byte[].class))).thenAnswer(invocation -> invocation.getArgument(0));
        byte[] result = rScriptService.executeRScriptAndRetrievePlot(resourceFile);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        Files.deleteIfExists(Path.of("." + File.separator + "modified_testfile.R"));
    }

    @Test
    void should_throw_exception_when_providing_invalid_file() throws IOException {
        // Given
        Path invalidFilePath = Paths.get("non_existent_file.R");

        // When and Then
        IOException exception = assertThrows(IOException.class,
                () -> rScriptService.executeRScriptAndRetrievePlot(invalidFilePath));

        assertEquals("java.nio.file.NoSuchFileException", exception.getClass().getName());
        assertTrue(exception.getMessage().contains("non_existent_file.R"));

        verify(rProcessor, never()).execute(any(), any(), any());
        verify(documentService, never()).generateWord(any(byte[].class));
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
        byte[] pngContent = "PNG Content".getBytes(StandardCharsets.UTF_8);
        Files.write(Path.of(pngFilePath), pngContent, StandardOpenOption.CREATE);

        // When
        Process mockedProcess = mock(Process.class);
        when(rProcessor.execute(any(), any(), any())).thenReturn(mockedProcess);
        InputStream inputStream = new ByteArrayInputStream("Process Output".getBytes(StandardCharsets.UTF_8));
        InputStream errorStream = new ByteArrayInputStream("Process Error".getBytes(StandardCharsets.UTF_8));
        when(mockedProcess.getInputStream()).thenReturn(inputStream);
        when(mockedProcess.getErrorStream()).thenReturn(errorStream);
        when(mockedProcess.waitFor()).thenReturn(0);
        when(documentService.generateWord(any(byte[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        byte[] result = rScriptService.createPlotFromRScript(multipartFile);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);

        Files.deleteIfExists(Path.of(pngFilePath));
        Files.deleteIfExists(Path.of(originalFileName));
        Files.deleteIfExists(Path.of(modifiedFileName));
    }
}
