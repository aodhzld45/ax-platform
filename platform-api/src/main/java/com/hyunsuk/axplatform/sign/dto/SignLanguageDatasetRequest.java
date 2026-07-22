package com.hyunsuk.axplatform.sign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignLanguageDatasetRequest {

    @NotNull
    private Long documentId;

    @Size(max = 100)
    private String datasetType;

    @Size(max = 500)
    private String description;
}
