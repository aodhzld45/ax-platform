package com.hyunsuk.axplatform.document.controller;

import com.hyunsuk.axplatform.document.dto.DocumentListResponse;
import com.hyunsuk.axplatform.document.dto.DocumentResponse;
import com.hyunsuk.axplatform.document.dto.DocumentUploadRequest;
import com.hyunsuk.axplatform.document.dto.DocumentUploadResponse;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import com.hyunsuk.axplatform.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<DocumentListResponse> findAll(
            @RequestParam(required = false)
            DocumentStatus documentStatus,
            @RequestParam(required = false)
            DocumentIndexStatus indexStatus,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                documentService.findAll(
                        documentStatus,
                        indexStatus,
                        pageable
                )
        );
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> findById(
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(documentService.findById(documentId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> upload(
            @Valid @ModelAttribute DocumentUploadRequest request
    ) {
        DocumentUploadResponse response =
                documentService.upload(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/documents/" + response.getDocumentId()
                ))
                .body(response);
    }
}
