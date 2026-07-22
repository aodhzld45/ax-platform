package com.hyunsuk.axplatform.medical.exception;

import lombok.Getter;

@Getter
public class MedicalManualRegistrationException extends RuntimeException {

    private final String errorCode;

    public MedicalManualRegistrationException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
