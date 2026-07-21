package com.hyunsuk.axplatform.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public static ErrorResponse of(
            String errorCode,
            String message,
            String path
    ) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
