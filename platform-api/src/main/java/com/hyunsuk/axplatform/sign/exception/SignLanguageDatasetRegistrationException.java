package com.hyunsuk.axplatform.sign.exception;

import lombok.Getter;

@Getter
public class SignLanguageDatasetRegistrationException
        extends RuntimeException {

    private final String errorCode;

    public SignLanguageDatasetRegistrationException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
