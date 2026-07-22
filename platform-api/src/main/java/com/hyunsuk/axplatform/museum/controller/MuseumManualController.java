package com.hyunsuk.axplatform.museum.controller;

import com.hyunsuk.axplatform.museum.dto.MuseumManualRequest;
import com.hyunsuk.axplatform.museum.dto.MuseumManualResponse;
import com.hyunsuk.axplatform.museum.service.MuseumManualService;
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
@RequestMapping("/api/v1/museum-manuals")
public class MuseumManualController {

    private final MuseumManualService museumManualService;

    @PostMapping
    public ResponseEntity<MuseumManualResponse> register(
            @Valid @RequestBody MuseumManualRequest request
    ) {
        MuseumManualResponse response =
                museumManualService.register(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/museum-manuals/"
                                + response.getMuseumManualId()
                ))
                .body(response);
    }
}
