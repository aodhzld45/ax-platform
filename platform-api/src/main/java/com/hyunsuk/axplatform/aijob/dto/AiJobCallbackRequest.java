package com.hyunsuk.axplatform.aijob.dto;

import com.hyunsuk.axplatform.aijob.entity.AiJobStage;
import com.hyunsuk.axplatform.aijob.entity.AiJobStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiJobCallbackRequest {

    private AiJobStatus status;
    private AiJobStage stage;
    private Integer progress;
    private String message;
    private String errorCode;
    private String errorMessage;
    private String resultJson;
}
