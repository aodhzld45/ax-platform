package com.hyunsuk.axplatform.museum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MuseumManualRequest {

    @NotNull
    private Long documentId;

    @Size(max = 150)
    private String museumName;

    @Size(max = 100)
    private String manualCategory;
}
