package com.hyunsuk.axplatform.museum.controller;

import com.hyunsuk.axplatform.museum.dto.MuseumManualListResponse;
import com.hyunsuk.axplatform.museum.dto.MuseumManualRequest;
import com.hyunsuk.axplatform.museum.dto.MuseumManualResponse;
import com.hyunsuk.axplatform.museum.service.MuseumManualService;
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
@RequestMapping("/api/v1/museum-manuals")
public class MuseumManualController {

    private final MuseumManualService museumManualService;

    @GetMapping
    public ResponseEntity<MuseumManualListResponse> findAll(
            Pageable pageable
    ) {
        return ResponseEntity.ok(museumManualService.findAll(pageable));
    }

    @GetMapping("/{museumManualId}")
    public ResponseEntity<MuseumManualResponse> findById(
            @PathVariable Long museumManualId
    ) {
        return ResponseEntity.ok(
                museumManualService.findById(museumManualId)
        );
    }

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
