package com.hyunsuk.axplatform.aijob.controller;

import com.hyunsuk.axplatform.aijob.dto.AiJobCreateRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobListResponse;
import com.hyunsuk.axplatform.aijob.dto.AiJobResponse;
import com.hyunsuk.axplatform.aijob.service.AiJobService;
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
@RequestMapping("/api/v1")
public class AiJobController {

    private final AiJobService aiJobService;

    @PostMapping("/documents/{documentId}/ai-jobs")
    public ResponseEntity<AiJobResponse> createPendingJob(
            @PathVariable Long documentId,
            @RequestBody(required = false) AiJobCreateRequest request
    ) {
        AiJobResponse response =
                aiJobService.createPendingJob(documentId, request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/ai-jobs/" + response.getJobKey()
                ))
                .body(response);
    }

    @GetMapping("/ai-jobs/{jobKey}")
    public ResponseEntity<AiJobResponse> findByJobKey(
            @PathVariable String jobKey
    ) {
        return ResponseEntity.ok(aiJobService.findByJobKey(jobKey));
    }

    @GetMapping("/documents/{documentId}/ai-jobs")
    public ResponseEntity<AiJobListResponse> findAllByDocumentId(
            @PathVariable Long documentId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                aiJobService.findAllByDocumentId(documentId, pageable)
        );
    }
}
