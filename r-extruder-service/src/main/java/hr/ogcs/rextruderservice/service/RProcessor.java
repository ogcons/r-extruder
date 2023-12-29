package hr.ogcs.rextruderservice.service;

import java.io.IOException;

public interface RProcessor {
    Process execute(String command) throws IOException;
}