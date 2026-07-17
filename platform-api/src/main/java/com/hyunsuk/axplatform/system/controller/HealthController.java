package com.hyunsuk.axplatform.system.controller;

import com.hyunsuk.axplatform.system.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse(
                "platform-api",
                "UP",
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}
