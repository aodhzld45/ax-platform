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
import jakarta.persistence.Lob;
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

    @Column(name = "max_retry_count", nullable = false)
    private int maxRetryCount;

    @Lob
    @Column(name = "result_json")
    private String resultJson;

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
            Integer maxRetryCount,
            String resultJson,
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
        this.maxRetryCount = maxRetryCount == null ? 3 : maxRetryCount;
        this.resultJson = resultJson;
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
                .maxRetryCount(3)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public void start() {
        validateTransition(AiJobStatus.PROCESSING);

        this.status = AiJobStatus.PROCESSING;
        this.currentStage = AiJobStage.TEXT_EXTRACTION;
        this.progress = 10;
        this.startedAt = LocalDateTime.now();
    }

    public void updateProgress(int progress) {
        if (this.status != AiJobStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 작업만 진행률을 변경할 수 있습니다."
            );
        }

        if (progress < 0 || progress >= 100) {
            throw new IllegalArgumentException(
                    "진행률은 0 이상 100 미만이어야 합니다."
            );
        }

        this.progress = progress;
    }

    public void updateStage(AiJobStage stage, int progress) {
        if (this.status != AiJobStatus.PROCESSING) {
            throw new IllegalStateException(
                    "처리 중인 작업만 단계를 변경할 수 있습니다."
            );
        }

        validateStageForward(stage);

        if (progress < this.progress) {
            throw new IllegalStateException(
                    "AiJob 진행률은 이전 값보다 낮아질 수 없습니다. "
                            + this.progress + " -> " + progress
            );
        }

        updateProgress(progress);
        this.currentStage = stage;
    }

    public void complete(String resultJson) {
        complete(AiJobStage.RESULT_FINALIZATION, resultJson);
    }

    public void complete(AiJobStage stage, String resultJson) {
        validateTransition(AiJobStatus.COMPLETED);
        validateStageForward(stage);

        this.status = AiJobStatus.COMPLETED;
        this.currentStage = stage;
        this.progress = 100;
        this.resultJson = resultJson;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorCode, String errorMessage) {
        fail(this.currentStage, errorCode, errorMessage);
    }

    public void fail(
            AiJobStage stage,
            String errorCode,
            String errorMessage
    ) {
        validateTransition(AiJobStatus.FAILED);
        validateStageForward(stage);

        this.status = AiJobStatus.FAILED;
        this.currentStage = stage;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void retry() {
        validateTransition(AiJobStatus.RETRYING);

        if (this.retryCount >= this.maxRetryCount) {
            throw new IllegalStateException(
                    "최대 재시도 횟수를 초과했습니다."
            );
        }

        this.status = AiJobStatus.RETRYING;
        this.retryCount++;
        this.progress = 0;
        this.errorCode = null;
        this.errorMessage = null;
        this.completedAt = null;
    }

    public void cancel() {
        validateTransition(AiJobStatus.CANCELLED);

        this.status = AiJobStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    private void validateTransition(AiJobStatus targetStatus) {
        boolean allowed = switch (this.status) {
            case PENDING ->
                    targetStatus == AiJobStatus.PROCESSING
                            || targetStatus == AiJobStatus.FAILED
                            || targetStatus == AiJobStatus.CANCELLED;
            case PROCESSING ->
                    targetStatus == AiJobStatus.COMPLETED
                            || targetStatus == AiJobStatus.FAILED
                            || targetStatus == AiJobStatus.CANCELLED;
            case FAILED ->
                    targetStatus == AiJobStatus.RETRYING
                            || targetStatus == AiJobStatus.CANCELLED;
            case RETRYING ->
                    targetStatus == AiJobStatus.PROCESSING
                            || targetStatus == AiJobStatus.FAILED
                            || targetStatus == AiJobStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "허용되지 않은 AiJob 상태 전이입니다. "
                            + this.status + " -> " + targetStatus
            );
        }
    }

    private void validateStageForward(AiJobStage targetStage) {
        if (targetStage == null) {
            throw new IllegalArgumentException("AiJob 단계는 필수입니다.");
        }

        if (targetStage.ordinal() < this.currentStage.ordinal()) {
            throw new IllegalStateException(
                    "AiJob 단계는 이전 단계로 되돌릴 수 없습니다. "
                            + this.currentStage + " -> " + targetStage
            );
        }
    }
}
