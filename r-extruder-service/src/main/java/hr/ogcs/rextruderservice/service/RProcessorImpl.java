package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Executes the R executable which creates png plot
 */
@Profile("default")
@Service
@Slf4j
public class RProcessorImpl implements RProcessor {
    @Override
    public Process execute(String command) throws IOException {
        log.info("Executing command: {} ", command);
        return Runtime.getRuntime().exec(command);
    }
}
