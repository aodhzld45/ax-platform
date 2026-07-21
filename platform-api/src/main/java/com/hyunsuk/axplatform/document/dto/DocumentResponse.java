package com.hyunsuk.axplatform.document.dto;

import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentResponse {

    private final Long documentId;
    private final String resourceKey;
    private final Integer version;
    private final String title;
    private final DocumentStatus documentStatus;
    private final DocumentIndexStatus indexStatus;
    private final DocumentFileResponse file;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getId())
                .resourceKey(document.getResourceKey())
                .version(document.getVersion())
                .title(document.getTitle())
                .documentStatus(document.getDocumentStatus())
                .indexStatus(document.getIndexStatus())
                .file(DocumentFileResponse.from(
                        document.getFileMetadata()
                ))
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
