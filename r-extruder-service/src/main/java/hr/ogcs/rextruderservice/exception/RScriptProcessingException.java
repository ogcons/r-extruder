package hr.ogcs.rextruderservice.exception;

import lombok.Getter;

@Getter
public class RScriptProcessingException extends RuntimeException {

    private final Exception exception;

    public RScriptProcessingException(Exception e) {
        super();
        this.exception = e;
    }
}
