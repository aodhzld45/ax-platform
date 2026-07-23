package com.hyunsuk.axplatform.museum.dto;

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
public class MuseumManualListResponse {

    private List<MuseumManualResponse> items;
    private long totalCount;
    private int totalPages;

    public static MuseumManualListResponse of(
            List<MuseumManualResponse> items,
            long totalCount,
            int totalPages
    ) {
        return MuseumManualListResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }
}
