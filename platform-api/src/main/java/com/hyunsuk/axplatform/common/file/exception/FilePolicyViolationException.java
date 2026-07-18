package com.hyunsuk.axplatform.common.file.exception;

import lombok.Getter;

@Getter
public class FilePolicyViolationException extends RuntimeException {

    private final String errorCode;

    public FilePolicyViolationException(
            String errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
