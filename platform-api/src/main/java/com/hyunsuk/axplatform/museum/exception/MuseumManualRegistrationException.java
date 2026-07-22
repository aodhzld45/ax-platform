package com.hyunsuk.axplatform.museum.exception;

import lombok.Getter;

@Getter
public class MuseumManualRegistrationException extends RuntimeException {

    private final String errorCode;

    public MuseumManualRegistrationException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
