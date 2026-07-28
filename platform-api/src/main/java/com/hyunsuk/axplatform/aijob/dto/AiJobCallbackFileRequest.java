package com.hyunsuk.axplatform.aijob.dto;

import com.hyunsuk.axplatform.aijob.entity.AiJobFileRole;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiJobCallbackFileRequest {

    private AiJobFileRole role;
    private FileAssetType assetType;
    private String originalFileName;
    private String storedFileName;
    private String extension;
    private String contentType;
    private long fileSize;
    private String storageRelativePath;
    private String accessPath;
    private String checksumSha256;
}
