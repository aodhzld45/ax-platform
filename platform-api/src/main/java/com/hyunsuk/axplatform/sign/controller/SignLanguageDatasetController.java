package com.hyunsuk.axplatform.sign.controller;

import com.hyunsuk.axplatform.sign.dto.SignLanguageDatasetRequest;
import com.hyunsuk.axplatform.sign.dto.SignLanguageDatasetResponse;
import com.hyunsuk.axplatform.sign.service.SignLanguageDatasetService;
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
@RequestMapping("/api/v1/sign-language-datasets")
public class SignLanguageDatasetController {

    private final SignLanguageDatasetService signLanguageDatasetService;

    @PostMapping
    public ResponseEntity<SignLanguageDatasetResponse> register(
            @Valid @RequestBody SignLanguageDatasetRequest request
    ) {
        SignLanguageDatasetResponse response =
                signLanguageDatasetService.register(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/sign-language-datasets/"
                                + response.getSignLanguageDatasetId()
                ))
                .body(response);
    }
}
