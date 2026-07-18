package com.hyunsuk.axplatform.common.file.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 원본 파일명, UUID 파일명, 크기, 확장자까지 DB에 저장해야 하므로 결과 객체를 반환
@Getter
@Builder
@Setter
@ToString
public class StoredFileInfo {

    private String originalFilename;

    private String storedFilename;

    /**
     * DB 또는 외부 응답에 사용할 경로
     * 예: /files/document/korean-source/DOC_001/original/UUID.pdf
     */
    private String storedPath;

    private String storageRelativePath;

    private String accessPath;

    private String extension;

    private String contentType;

    private long fileSize;
    
}
