package com.hyunsuk.axplatform.common.exception;

import com.hyunsuk.axplatform.aijob.exception.AiJobNotFoundException;
import com.hyunsuk.axplatform.common.file.exception.FileMetadataNotFoundException;
import com.hyunsuk.axplatform.common.file.exception.FilePolicyViolationException;
import com.hyunsuk.axplatform.common.file.exception.StoredFileNotFoundException;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.korean.exception.KoreanSourceDocumentRegistrationException;
import com.hyunsuk.axplatform.medical.exception.MedicalManualRegistrationException;
import com.hyunsuk.axplatform.museum.exception.MuseumManualRegistrationException;
import com.hyunsuk.axplatform.sign.exception.SignLanguageDatasetRegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FilePolicyViolationException.class)
    public ResponseEntity<ErrorResponse> handleFilePolicyViolation(
            FilePolicyViolationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        exception.getErrorCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(
            DocumentNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "DOCUMENT_NOT_FOUND",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(AiJobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAiJobNotFound(
            AiJobNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "AI_JOB_NOT_FOUND",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(FileMetadataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileMetadataNotFound(
            FileMetadataNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "FILE_METADATA_NOT_FOUND",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(StoredFileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoredFileNotFound(
            StoredFileNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "STORED_FILE_NOT_FOUND",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(KoreanSourceDocumentRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleKoreanSourceDocumentRegistration(
            KoreanSourceDocumentRegistrationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        exception.getErrorCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(SignLanguageDatasetRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleSignLanguageDatasetRegistration(
            SignLanguageDatasetRegistrationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        exception.getErrorCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MuseumManualRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleMuseumManualRegistration(
            MuseumManualRegistrationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        exception.getErrorCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MedicalManualRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleMedicalManualRegistration(
            MedicalManualRegistrationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        exception.getErrorCode(),
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .orElse("Invalid request.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "VALIDATION_ERROR",
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "BAD_REQUEST",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        "AI_JOB_STATE_CONFLICT",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }
}
