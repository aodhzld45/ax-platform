package com.hyunsuk.axplatform.medical.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MedicalManualRequest {

    @NotNull
    private Long documentId;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String manualCategory;
}
