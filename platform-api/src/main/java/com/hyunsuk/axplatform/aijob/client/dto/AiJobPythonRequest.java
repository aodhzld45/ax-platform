package com.hyunsuk.axplatform.aijob.client.dto;

import com.hyunsuk.axplatform.aijob.entity.AiJob;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiJobPythonRequest {

    private final String jobId;
    private final Long documentId;
    private final Long fileMetadataId;
    private final String jobType;
    private final String storageRelativePath;
    private final String accessPath;
    private final String callbackUrl;

    public static AiJobPythonRequest from(AiJob aiJob) {
        return AiJobPythonRequest.builder()
                .jobId(aiJob.getJobKey())
                .documentId(aiJob.getDocument().getId())
                .fileMetadataId(aiJob.getDocument().getFileMetadata().getId())
                .jobType(aiJob.getJobType().name())
                .storageRelativePath(
                        aiJob.getDocument()
                                .getFileMetadata()
                                .getStorageRelativePath()
                )
                .accessPath(
                        aiJob.getDocument()
                                .getFileMetadata()
                                .getAccessPath()
                )
                .callbackUrl(
                        "/api/v1/ai-jobs/"
                                + aiJob.getJobKey()
                                + "/callback"
                )
                .build();
    }
}
