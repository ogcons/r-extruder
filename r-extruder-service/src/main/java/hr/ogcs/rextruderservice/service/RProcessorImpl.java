package hr.ogcs.rextruderservice.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Profile("default")
@Service
public class RProcessorImpl implements RProcessor {
    @Override
    public Process execute(String command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }
}
