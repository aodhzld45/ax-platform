package com.hyunsuk.axplatform.medical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalManualListResponse {

    private List<MedicalManualResponse> items;
    private long totalCount;
    private int totalPages;

    public static MedicalManualListResponse of(
            List<MedicalManualResponse> items,
            long totalCount,
            int totalPages
    ) {
        return MedicalManualListResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }
}
