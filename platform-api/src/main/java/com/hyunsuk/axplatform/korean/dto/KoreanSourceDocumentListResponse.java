package com.hyunsuk.axplatform.korean.dto;

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
public class KoreanSourceDocumentListResponse {

    private List<KoreanSourceDocumentResponse> items;
    private long totalCount;
    private int totalPages;

    public static KoreanSourceDocumentListResponse of(
            List<KoreanSourceDocumentResponse> items,
            long totalCount,
            int totalPages
    ) {
        return KoreanSourceDocumentListResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }
}
