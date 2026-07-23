package com.hyunsuk.axplatform.document.dto;

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
public class DocumentListResponse {

    private List<DocumentResponse> items;
    private long totalCount;
    private int totalPages;

    public static DocumentListResponse of(
            List<DocumentResponse> items,
            long totalCount,
            int totalPages
    ) {
        return DocumentListResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }
}
