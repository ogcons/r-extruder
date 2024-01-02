package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Executes the R executable which creates png plot
 */
@Profile("default")
@Component
@Slf4j
public class RProcessorImpl implements RProcessor {
    @Override
    public Process execute(String command, String outputFileName, Path destinationPath) throws IOException {
        log.info("Executing command: {} ", command);
        return Runtime.getRuntime().exec(command);
    }
}
