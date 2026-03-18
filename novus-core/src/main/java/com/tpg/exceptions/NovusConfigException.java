package com.tpg.exceptions;

public class NovusConfigException extends RuntimeException {

    public NovusConfigException(String message) {
        super(message);
    }

    public NovusConfigException(Throwable cause) {
        super(cause);
    }
}
