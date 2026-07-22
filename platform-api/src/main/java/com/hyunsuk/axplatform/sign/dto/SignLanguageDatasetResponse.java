package com.hyunsuk.axplatform.sign.dto;

import com.hyunsuk.axplatform.document.dto.DocumentFileResponse;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import com.hyunsuk.axplatform.sign.entity.SignLanguageDataset;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignLanguageDatasetResponse {

    private final Long signLanguageDatasetId;
    private final Long documentId;
    private final String resourceKey;
    private final Integer version;
    private final String title;
    private final DocumentStatus documentStatus;
    private final DocumentIndexStatus indexStatus;
    private final String datasetType;
    private final String description;
    private final DocumentFileResponse file;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static SignLanguageDatasetResponse from(
            SignLanguageDataset signLanguageDataset
    ) {
        var document = signLanguageDataset.getDocument();

        return SignLanguageDatasetResponse.builder()
                .signLanguageDatasetId(signLanguageDataset.getId())
                .documentId(document.getId())
                .resourceKey(document.getResourceKey())
                .version(document.getVersion())
                .title(document.getTitle())
                .documentStatus(document.getDocumentStatus())
                .indexStatus(document.getIndexStatus())
                .datasetType(signLanguageDataset.getDatasetType())
                .description(signLanguageDataset.getDescription())
                .file(DocumentFileResponse.from(
                        document.getFileMetadata()
                ))
                .createdAt(signLanguageDataset.getCreatedAt())
                .updatedAt(signLanguageDataset.getUpdatedAt())
                .build();
    }
}
