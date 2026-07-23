package com.hyunsuk.axplatform.korean.controller;

import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentListResponse;
import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentRequest;
import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentResponse;
import com.hyunsuk.axplatform.korean.service.KoreanSourceDocumentService;
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
@RequestMapping("/api/v1/korean-source-documents")
public class KoreanSourceDocumentController {

    private final KoreanSourceDocumentService koreanSourceDocumentService;

    @GetMapping
    public ResponseEntity<KoreanSourceDocumentListResponse> findAll(
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                koreanSourceDocumentService.findAll(pageable)
        );
    }

    @GetMapping("/{koreanSourceDocumentId}")
    public ResponseEntity<KoreanSourceDocumentResponse> findById(
            @PathVariable Long koreanSourceDocumentId
    ) {
        return ResponseEntity.ok(
                koreanSourceDocumentService.findById(
                        koreanSourceDocumentId
                )
        );
    }

    @PostMapping
    public ResponseEntity<KoreanSourceDocumentResponse> register(
            @Valid @RequestBody KoreanSourceDocumentRequest request
    ) {
        KoreanSourceDocumentResponse response =
                koreanSourceDocumentService.register(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/korean-source-documents/"
                                + response.getKoreanSourceDocumentId()
                ))
                .body(response);
    }
}
