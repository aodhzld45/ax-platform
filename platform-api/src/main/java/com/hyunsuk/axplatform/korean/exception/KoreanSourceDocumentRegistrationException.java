package com.hyunsuk.axplatform.korean.exception;

import lombok.Getter;

@Getter
public class KoreanSourceDocumentRegistrationException
        extends RuntimeException {

    private final String errorCode;

    public KoreanSourceDocumentRegistrationException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
