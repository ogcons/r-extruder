package hr.ogcs.rextruderservice.service;

import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Profile("default")
public class RProcessorImpl implements RProcessor {
    @Override
    public Process execute(String command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }
}
