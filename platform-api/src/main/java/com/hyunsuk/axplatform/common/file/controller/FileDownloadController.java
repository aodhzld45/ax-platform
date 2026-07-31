package com.hyunsuk.axplatform.common.file.controller;

import com.hyunsuk.axplatform.common.file.dto.FileDownloadResource;
import com.hyunsuk.axplatform.common.file.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    @GetMapping("/{fileMetadataId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long fileMetadataId
    ) {
        FileDownloadResource downloadResource =
                fileDownloadService.findDownloadResource(fileMetadataId);

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

    private MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
