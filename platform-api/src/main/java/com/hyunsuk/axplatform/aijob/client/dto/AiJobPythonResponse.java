package com.hyunsuk.axplatform.aijob.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiJobPythonResponse {

    private String jobId;
    private boolean accepted;
    private String status;
    private String message;
}
