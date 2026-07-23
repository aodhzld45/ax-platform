package com.hyunsuk.axplatform.sign.dto;

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
public class SignLanguageDatasetListResponse {

    private List<SignLanguageDatasetResponse> items;
    private long totalCount;
    private int totalPages;

    public static SignLanguageDatasetListResponse of(
            List<SignLanguageDatasetResponse> items,
            long totalCount,
            int totalPages
    ) {
        return SignLanguageDatasetListResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }
}
