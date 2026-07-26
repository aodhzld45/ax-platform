package com.hyunsuk.axplatform.aijob.dto;

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
public class AiJobListResponse {

    private List<AiJobResponse> items;
    private long totalCount;
    private int totalPages;

    public static AiJobListResponse of(
            List<AiJobResponse> items,
            long totalCount,
            int totalPages
    ) {
        return AiJobListResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }
}
