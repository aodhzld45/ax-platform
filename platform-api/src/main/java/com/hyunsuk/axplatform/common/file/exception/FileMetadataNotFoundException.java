package com.hyunsuk.axplatform.common.file.exception;

public class FileMetadataNotFoundException extends RuntimeException {

    public FileMetadataNotFoundException(Long fileMetadataId) {
        super("파일 메타데이터를 찾을 수 없습니다. fileMetadataId="
                + fileMetadataId);
    }
}
