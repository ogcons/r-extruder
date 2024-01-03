package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

@Profile("default")
@Component
@Slf4j
public class RProcessorImpl implements RProcessor {

    @Override
    public Process execute(String command, String outputFileName, Path destinationPath) throws IOException {
        log.info("Executing command: {} ", command);

        // Split the command into an array of strings
        String[] commandArray = command.split("\\s+");

        // Set the working directory to the uploadDir
        ProcessBuilder processBuilder = new ProcessBuilder()
                .command(Arrays.asList(commandArray))
                .directory(destinationPath.toFile());

        return processBuilder.start();
    }
}