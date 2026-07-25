package com.hyunsuk.axplatform.system.controller;

import com.hyunsuk.axplatform.system.dto.SystemServicesResponse;
import com.hyunsuk.axplatform.system.service.SystemServiceStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system")
public class SystemServiceController {

    private final SystemServiceStatusService systemServiceStatusService;

    @GetMapping("/services")
    public ResponseEntity<SystemServicesResponse> getServicesStatus() {
        return ResponseEntity.ok(
                systemServiceStatusService.getServicesStatus()
        );
    }
}
