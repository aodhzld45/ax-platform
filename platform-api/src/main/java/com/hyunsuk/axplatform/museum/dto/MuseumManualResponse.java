package com.hyunsuk.axplatform.museum.dto;

import com.hyunsuk.axplatform.document.dto.DocumentFileResponse;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import com.hyunsuk.axplatform.museum.entity.MuseumManual;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MuseumManualResponse {

    private final Long museumManualId;
    private final Long documentId;
    private final String resourceKey;
    private final Integer version;
    private final String title;
    private final DocumentStatus documentStatus;
    private final DocumentIndexStatus indexStatus;
    private final String museumName;
    private final String manualCategory;
    private final DocumentFileResponse file;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static MuseumManualResponse from(MuseumManual museumManual) {
        var document = museumManual.getDocument();

        return MuseumManualResponse.builder()
                .museumManualId(museumManual.getId())
                .documentId(document.getId())
                .resourceKey(document.getResourceKey())
                .version(document.getVersion())
                .title(document.getTitle())
                .documentStatus(document.getDocumentStatus())
                .indexStatus(document.getIndexStatus())
                .museumName(museumManual.getMuseumName())
                .manualCategory(museumManual.getManualCategory())
                .file(DocumentFileResponse.from(
                        document.getFileMetadata()
                ))
                .createdAt(museumManual.getCreatedAt())
                .updatedAt(museumManual.getUpdatedAt())
                .build();
    }
}
