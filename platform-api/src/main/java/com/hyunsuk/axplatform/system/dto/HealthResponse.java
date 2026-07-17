package com.hyunsuk.axplatform.system.dto;

import java.time.LocalDateTime;

public record HealthResponse(
        String service,
        String status,
        LocalDateTime timestamp
) {
}
