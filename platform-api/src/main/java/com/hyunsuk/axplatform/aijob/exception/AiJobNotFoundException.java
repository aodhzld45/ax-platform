package com.hyunsuk.axplatform.aijob.exception;

public class AiJobNotFoundException extends RuntimeException {

    public AiJobNotFoundException(String jobKey) {
        super("AI job not found. jobKey=" + jobKey);
    }
}
