package com.tpg.exceptions;


import com.tpg.services.NovusLoggerService;

public class NovusUiException extends RuntimeException {

    final NovusLoggerService log = NovusLoggerService.init(NovusUiException.class);

    public NovusUiException(String message) {
        super(message);
        log.uiException(message);
    }
}
