package com.hyunsuk.axplatform.medical.controller;

import com.hyunsuk.axplatform.medical.dto.MedicalManualListResponse;
import com.hyunsuk.axplatform.medical.dto.MedicalManualRequest;
import com.hyunsuk.axplatform.medical.dto.MedicalManualResponse;
import com.hyunsuk.axplatform.medical.service.MedicalManualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/medical-manuals")
public class MedicalManualController {

    private final MedicalManualService medicalManualService;

    @GetMapping
    public ResponseEntity<MedicalManualListResponse> findAll(
            Pageable pageable
    ) {
        return ResponseEntity.ok(medicalManualService.findAll(pageable));
    }

    @GetMapping("/{medicalManualId}")
    public ResponseEntity<MedicalManualResponse> findById(
            @PathVariable Long medicalManualId
    ) {
        return ResponseEntity.ok(
                medicalManualService.findById(medicalManualId)
        );
    }

    @PostMapping
    public ResponseEntity<MedicalManualResponse> register(
            @Valid @RequestBody MedicalManualRequest request
    ) {
        MedicalManualResponse response =
                medicalManualService.register(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/medical-manuals/"
                                + response.getMedicalManualId()
                ))
                .body(response);
    }
}
