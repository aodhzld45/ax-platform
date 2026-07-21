package com.hyunsuk.axplatform.document.dto;

import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentUploadResponse {

    private Long documentId;

    private String resourceKey;

    private Integer version;

    private String title;

    private String originalFileName;

    private String accessPath;

    private DocumentStatus documentStatus;

    private DocumentIndexStatus indexStatus;
}
