package com.hyunsuk.axplatform.aijob.controller;

import com.hyunsuk.axplatform.aijob.dto.AiJobCreateRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobCallbackRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobFileListResponse;
import com.hyunsuk.axplatform.aijob.dto.AiJobListResponse;
import com.hyunsuk.axplatform.aijob.dto.AiJobResponse;
import com.hyunsuk.axplatform.aijob.service.AiJobService;
import com.hyunsuk.axplatform.common.file.dto.FileDownloadResource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;

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

    @GetMapping("/ai-jobs/{jobKey}/files")
    public ResponseEntity<AiJobFileListResponse> findFilesByJobKey(
            @PathVariable String jobKey
    ) {
        return ResponseEntity.ok(
                aiJobService.findFilesByJobKey(jobKey)
        );
    }

    @GetMapping("/ai-jobs/{jobKey}/files/{aiJobFileId}/download")
    public ResponseEntity<Resource> downloadJobFile(
            @PathVariable String jobKey,
            @PathVariable Long aiJobFileId
    ) {
        FileDownloadResource downloadResource =
                aiJobService.findJobFileDownloadResource(
                        jobKey,
                        aiJobFileId
                );

        return ResponseEntity.ok()
                .contentType(parseMediaType(
                        downloadResource.getContentType()
                ))
                .contentLength(downloadResource.getContentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(
                                        downloadResource.getOriginalFileName(),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                                .toString()
                )
                .body(downloadResource.getResource());
    }

    @PatchMapping("/ai-jobs/{jobKey}/callback")
    public ResponseEntity<AiJobResponse> handleCallback(
            @PathVariable String jobKey,
            @RequestBody AiJobCallbackRequest request
    ) {
        return ResponseEntity.ok(
                aiJobService.handleCallback(jobKey, request)
        );
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

    private MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
