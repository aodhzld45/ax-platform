package com.hyunsuk.axplatform.system.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceStatusResponse {

    private final String status;
    private final Long latencyMs;
    private final String errorCode;
    private final String message;

    public static ServiceStatusResponse up(Long latencyMs) {
        return ServiceStatusResponse.builder()
                .status("UP")
                .latencyMs(latencyMs)
                .build();
    }

    public static ServiceStatusResponse down(
            Long latencyMs,
            String errorCode,
            String message
    ) {
        return ServiceStatusResponse.builder()
                .status("DOWN")
                .latencyMs(latencyMs)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
