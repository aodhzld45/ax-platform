package com.hyunsuk.axplatform.common.file.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
public class FileDownloadResource {

    private final Resource resource;
    private final String originalFileName;
    private final String contentType;
    private final long contentLength;
}
