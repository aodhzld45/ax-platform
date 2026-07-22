package com.hyunsuk.axplatform.korean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KoreanSourceDocumentRequest {

    @NotNull
    private Long documentId;

    @Size(max = 100)
    private String sourceDomain;

    @Size(max = 500)
    private String description;
}
