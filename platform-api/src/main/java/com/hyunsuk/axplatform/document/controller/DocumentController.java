package com.hyunsuk.axplatform.document.controller;

import com.hyunsuk.axplatform.common.file.exception.FilePolicyViolationException;
import com.hyunsuk.axplatform.document.dto.DocumentUploadRequest;
import com.hyunsuk.axplatform.document.dto.DocumentUploadResponse;
import com.hyunsuk.axplatform.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

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

    @ExceptionHandler(FilePolicyViolationException.class)
    public ResponseEntity<Map<String, String>> handleFilePolicyViolation(
            FilePolicyViolationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "errorCode",
                        exception.getErrorCode(),
                        "message",
                        exception.getMessage()
                ));
    }
}
