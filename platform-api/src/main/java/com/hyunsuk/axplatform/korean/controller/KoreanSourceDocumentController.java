package com.hyunsuk.axplatform.korean.controller;

import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentRequest;
import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentResponse;
import com.hyunsuk.axplatform.korean.service.KoreanSourceDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
