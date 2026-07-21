package com.hyunsuk.axplatform.document.dto;

import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.entity.FileMetadataStatus;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentFileResponse {

    private final Long fileMetadataId;
    private final FileAssetType assetType;
    private final FileMetadataStatus status;
    private final String originalFileName;
    private final String storedFileName;
    private final String extension;
    private final String contentType;
    private final long fileSize;
    private final String storageRelativePath;
    private final String accessPath;
    private final String checksumSha256;

    public static DocumentFileResponse from(FileMetadata fileMetadata) {
        return DocumentFileResponse.builder()
                .fileMetadataId(fileMetadata.getId())
                .assetType(fileMetadata.getAssetType())
                .status(fileMetadata.getStatus())
                .originalFileName(fileMetadata.getOriginalFileName())
                .storedFileName(fileMetadata.getStoredFileName())
                .extension(fileMetadata.getExtension())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .storageRelativePath(fileMetadata.getStorageRelativePath())
                .accessPath(fileMetadata.getAccessPath())
                .checksumSha256(fileMetadata.getChecksumSha256())
                .build();
    }
}
