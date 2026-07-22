package com.hyunsuk.axplatform.medical.dto;

import com.hyunsuk.axplatform.document.dto.DocumentFileResponse;
import com.hyunsuk.axplatform.document.entity.DocumentIndexStatus;
import com.hyunsuk.axplatform.document.entity.DocumentStatus;
import com.hyunsuk.axplatform.medical.entity.MedicalManual;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MedicalManualResponse {

    private final Long medicalManualId;
    private final Long documentId;
    private final String resourceKey;
    private final Integer version;
    private final String title;
    private final DocumentStatus documentStatus;
    private final DocumentIndexStatus indexStatus;
    private final String department;
    private final String manualCategory;
    private final DocumentFileResponse file;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static MedicalManualResponse from(MedicalManual medicalManual) {
        var document = medicalManual.getDocument();

        return MedicalManualResponse.builder()
                .medicalManualId(medicalManual.getId())
                .documentId(document.getId())
                .resourceKey(document.getResourceKey())
                .version(document.getVersion())
                .title(document.getTitle())
                .documentStatus(document.getDocumentStatus())
                .indexStatus(document.getIndexStatus())
                .department(medicalManual.getDepartment())
                .manualCategory(medicalManual.getManualCategory())
                .file(DocumentFileResponse.from(
                        document.getFileMetadata()
                ))
                .createdAt(medicalManual.getCreatedAt())
                .updatedAt(medicalManual.getUpdatedAt())
                .build();
    }
}
