package com.hyunsuk.axplatform.document.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long documentId) {
        super("Document not found. documentId=" + documentId);
    }
}
