package com.hyunsuk.axplatform.aijob.dto;

import com.hyunsuk.axplatform.aijob.entity.AiJob;
import com.hyunsuk.axplatform.aijob.entity.AiJobStage;
import com.hyunsuk.axplatform.aijob.entity.AiJobStatus;
import com.hyunsuk.axplatform.aijob.entity.AiJobType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiJobResponse {

    private final Long aiJobId;
    private final String jobKey;
    private final Long documentId;
    private final String documentTitle;
    private final AiJobType jobType;
    private final AiJobStatus status;
    private final AiJobStage currentStage;
    private final int progress;
    private final int retryCount;
    private final int maxRetryCount;
    private final String resultJson;
    private final String errorCode;
    private final String errorMessage;
    private final LocalDateTime requestedAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static AiJobResponse from(AiJob aiJob) {
        return AiJobResponse.builder()
                .aiJobId(aiJob.getId())
                .jobKey(aiJob.getJobKey())
                .documentId(aiJob.getDocument().getId())
                .documentTitle(aiJob.getDocument().getTitle())
                .jobType(aiJob.getJobType())
                .status(aiJob.getStatus())
                .currentStage(aiJob.getCurrentStage())
                .progress(aiJob.getProgress())
                .retryCount(aiJob.getRetryCount())
                .maxRetryCount(aiJob.getMaxRetryCount())
                .resultJson(aiJob.getResultJson())
                .errorCode(aiJob.getErrorCode())
                .errorMessage(aiJob.getErrorMessage())
                .requestedAt(aiJob.getRequestedAt())
                .startedAt(aiJob.getStartedAt())
                .completedAt(aiJob.getCompletedAt())
                .createdAt(aiJob.getCreatedAt())
                .updatedAt(aiJob.getUpdatedAt())
                .build();
    }
}
