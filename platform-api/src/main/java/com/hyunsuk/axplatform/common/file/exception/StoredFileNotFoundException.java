package com.hyunsuk.axplatform.common.file.exception;

public class StoredFileNotFoundException extends RuntimeException {

    public StoredFileNotFoundException(Long fileMetadataId) {
        super("저장된 파일을 찾을 수 없습니다. fileMetadataId="
                + fileMetadataId);
    }
}
