package com.hyunsuk.axplatform.aijob.exception;

public class AiJobFileNotFoundException extends RuntimeException {

    public AiJobFileNotFoundException(
            String jobKey,
            Long aiJobFileId
    ) {
        super("AiJob 산출물을 찾을 수 없습니다. jobKey="
                + jobKey
                + ", aiJobFileId="
                + aiJobFileId);
    }
}
