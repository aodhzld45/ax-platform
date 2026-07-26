package com.hyunsuk.axplatform.aijob.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiJobTest {

    @Test
    void startChangesPendingJobToProcessing() {
        AiJob aiJob = createPendingJob();

        aiJob.start();

        assertThat(aiJob.getStatus()).isEqualTo(AiJobStatus.PROCESSING);
        assertThat(aiJob.getStartedAt()).isNotNull();
    }

    @Test
    void updateProgressOnlyAllowsProcessingJobAndLessThan100() {
        AiJob aiJob = createPendingJob();

        assertThatThrownBy(() -> aiJob.updateProgress(10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("처리 중인 작업만 진행률을 변경할 수 있습니다.");

        aiJob.start();
        aiJob.updateProgress(40);

        assertThat(aiJob.getProgress()).isEqualTo(40);
        assertThatThrownBy(() -> aiJob.updateProgress(100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("진행률은 0 이상 100 미만이어야 합니다.");
    }

    @Test
    void completeChangesProcessingJobToCompleted() {
        AiJob aiJob = createPendingJob();
        aiJob.start();

        aiJob.complete("{\"glossCount\":3}");

        assertThat(aiJob.getStatus()).isEqualTo(AiJobStatus.COMPLETED);
        assertThat(aiJob.getProgress()).isEqualTo(100);
        assertThat(aiJob.getResultJson()).isEqualTo("{\"glossCount\":3}");
        assertThat(aiJob.getCompletedAt()).isNotNull();
    }

    @Test
    void failStoresErrorInformation() {
        AiJob aiJob = createPendingJob();
        aiJob.start();

        aiJob.fail("PYTHON_API_ERROR", "Python API 호출 실패");

        assertThat(aiJob.getStatus()).isEqualTo(AiJobStatus.FAILED);
        assertThat(aiJob.getErrorCode()).isEqualTo("PYTHON_API_ERROR");
        assertThat(aiJob.getErrorMessage()).isEqualTo("Python API 호출 실패");
        assertThat(aiJob.getCompletedAt()).isNotNull();
    }

    @Test
    void retryClearsFailureInformationAndAllowsRestart() {
        AiJob aiJob = createPendingJob();
        aiJob.start();
        aiJob.updateProgress(50);
        aiJob.fail("TEMPORARY_ERROR", "임시 오류");

        aiJob.retry();

        assertThat(aiJob.getStatus()).isEqualTo(AiJobStatus.RETRYING);
        assertThat(aiJob.getRetryCount()).isEqualTo(1);
        assertThat(aiJob.getProgress()).isZero();
        assertThat(aiJob.getErrorCode()).isNull();
        assertThat(aiJob.getErrorMessage()).isNull();
        assertThat(aiJob.getCompletedAt()).isNull();

        aiJob.start();

        assertThat(aiJob.getStatus()).isEqualTo(AiJobStatus.PROCESSING);
    }

    @Test
    void retryRejectsOverMaxRetryCount() {
        AiJob aiJob = AiJob.builder()
                .jobKey("JOB_TEST")
                .status(AiJobStatus.FAILED)
                .currentStage(AiJobStage.FILE_PREPARATION)
                .retryCount(3)
                .maxRetryCount(3)
                .build();

        assertThatThrownBy(aiJob::retry)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("최대 재시도 횟수를 초과했습니다.");
    }

    @Test
    void cancelChangesAllowedJobToCancelled() {
        AiJob aiJob = createPendingJob();

        aiJob.cancel();

        assertThat(aiJob.getStatus()).isEqualTo(AiJobStatus.CANCELLED);
        assertThat(aiJob.getCompletedAt()).isNotNull();
    }

    @Test
    void completedJobDoesNotAllowFurtherTransition() {
        AiJob aiJob = createPendingJob();
        aiJob.start();
        aiJob.complete("{}");

        assertThatThrownBy(aiJob::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("허용되지 않은 AiJob 상태 전이입니다. COMPLETED -> CANCELLED");
    }

    private AiJob createPendingJob() {
        return AiJob.createPending(
                "JOB_TEST",
                null,
                AiJobType.KOREAN_TO_GLOSS
        );
    }
}
