package com.hyunsuk.axplatform.korean.dto;

import com.hyunsuk.axplatform.document.dto.DocumentFileResponse;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import com.hyunsuk.axplatform.korean.entity.KoreanSourceDocument;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class KoreanSourceDocumentResponse {

    private final Long koreanSourceDocumentId;
    private final Long documentId;
    private final String resourceKey;
    private final Integer version;
    private final String title;
    private final DocumentStatus documentStatus;
    private final DocumentIndexStatus indexStatus;
    private final String sourceDomain;
    private final String description;
    private final DocumentFileResponse file;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static KoreanSourceDocumentResponse from(
            KoreanSourceDocument koreanSourceDocument
    ) {
        var document = koreanSourceDocument.getDocument();

        return KoreanSourceDocumentResponse.builder()
                .koreanSourceDocumentId(koreanSourceDocument.getId())
                .documentId(document.getId())
                .resourceKey(document.getResourceKey())
                .version(document.getVersion())
                .title(document.getTitle())
                .documentStatus(document.getDocumentStatus())
                .indexStatus(document.getIndexStatus())
                .sourceDomain(koreanSourceDocument.getSourceDomain())
                .description(koreanSourceDocument.getDescription())
                .file(DocumentFileResponse.from(
                        document.getFileMetadata()
                ))
                .createdAt(koreanSourceDocument.getCreatedAt())
                .updatedAt(koreanSourceDocument.getUpdatedAt())
                .build();
    }
}
