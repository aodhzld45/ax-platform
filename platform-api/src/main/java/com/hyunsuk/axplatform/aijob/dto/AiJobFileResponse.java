package com.hyunsuk.axplatform.aijob.dto;

import com.hyunsuk.axplatform.aijob.entity.AiJobFile;
import com.hyunsuk.axplatform.aijob.entity.AiJobFileRole;
import com.hyunsuk.axplatform.aijob.entity.AiJobStage;
import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiJobFileResponse {

    private final Long aiJobFileId;
    private final String jobKey;
    private final AiJobStage stage;
    private final AiJobFileRole role;
    private final Long fileMetadataId;
    private final FileAssetType assetType;
    private final String originalFileName;
    private final String storedFileName;
    private final String extension;
    private final String contentType;
    private final long fileSize;
    private final String storageRelativePath;
    private final String accessPath;
    private final LocalDateTime createdAt;

    public static AiJobFileResponse from(AiJobFile aiJobFile) {
        FileMetadata fileMetadata = aiJobFile.getFileMetadata();

        return AiJobFileResponse.builder()
                .aiJobFileId(aiJobFile.getId())
                .jobKey(aiJobFile.getAiJob().getJobKey())
                .stage(aiJobFile.getStage())
                .role(aiJobFile.getRole())
                .fileMetadataId(fileMetadata.getId())
                .assetType(fileMetadata.getAssetType())
                .originalFileName(fileMetadata.getOriginalFileName())
                .storedFileName(fileMetadata.getStoredFileName())
                .extension(fileMetadata.getExtension())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .storageRelativePath(fileMetadata.getStorageRelativePath())
                .accessPath(fileMetadata.getAccessPath())
                .createdAt(aiJobFile.getCreatedAt())
                .build();
    }
}
