package com.hyunsuk.axplatform.aijob.entity;

import com.hyunsuk.axplatform.common.entity.BaseTimeEntity;
import com.hyunsuk.axplatform.document.entity.Document;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ai_job")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_key", nullable = false, unique = true, length = 80)
    private String jobKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private AiJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AiJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 50)
    private AiJobStage currentStage;

    @Column(name = "progress", nullable = false)
    private int progress;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private AiJob(
            String jobKey,
            Document document,
            AiJobType jobType,
            AiJobStatus status,
            AiJobStage currentStage,
            Integer progress,
            Integer retryCount,
            String errorCode,
            String errorMessage,
            LocalDateTime requestedAt,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        this.jobKey = jobKey;
        this.document = document;
        this.jobType = jobType == null
                ? AiJobType.KOREAN_TO_GLOSS
                : jobType;
        this.status = status == null
                ? AiJobStatus.PENDING
                : status;
        this.currentStage = currentStage == null
                ? AiJobStage.FILE_PREPARATION
                : currentStage;
        this.progress = progress == null ? 0 : progress;
        this.retryCount = retryCount == null ? 0 : retryCount;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.requestedAt = requestedAt == null
                ? LocalDateTime.now()
                : requestedAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static AiJob createPending(
            String jobKey,
            Document document,
            AiJobType jobType
    ) {
        return AiJob.builder()
                .jobKey(jobKey)
                .document(document)
                .jobType(jobType)
                .status(AiJobStatus.PENDING)
                .currentStage(AiJobStage.FILE_PREPARATION)
                .progress(0)
                .retryCount(0)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}
