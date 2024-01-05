package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Executes R script in external process to create png for further document processing.
 * The PNG plot and the target PNG file are defined in the modified R script.
 */
@Profile("default")
@Service
@Slf4j
public class RProcessorImpl implements RProcessor {
    /**
     * Creates process object which executes the given command.
     * @param command The Rscript command
     * @param outputFileName R script file name
     * @param destinationPath Working directory
     * @return Executing process of the R script
     * @throws IOException Can happen if command is not executable
     */
    @Override
    public Process execute(String command, String outputFileName, Path destinationPath) throws IOException {
        log.info("Executing command: {}, filename: {}, path: {} ", command, outputFileName, destinationPath);

        // Split the command into an array of strings
        String[] commandArray = command.split("\\s+");

        // Set the working directory to the uploadDir
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command(Arrays.asList(commandArray))
                .directory(destinationPath.toFile());

        return processBuilder.start();
    }
}