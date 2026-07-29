package com.hyunsuk.axplatform.aijob.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiJobFileListResponse {

    private final List<AiJobFileResponse> items;
    private final long totalCount;

    public static AiJobFileListResponse of(
            List<AiJobFileResponse> items
    ) {
        return AiJobFileListResponse.builder()
                .items(items)
                .totalCount(items.size())
                .build();
    }
}
