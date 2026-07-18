package com.hyunsuk.axplatform.common.file.validator;

import com.hyunsuk.axplatform.common.file.type.*;
import com.hyunsuk.axplatform.common.file.exception.FilePolicyViolationException;
import com.hyunsuk.axplatform.common.file.policy.FileUploadPolicy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Component
public class FileUploadValidator {

    public void validate(
            MultipartFile file,
            FileAssetType assetType
    ) {
        validateFileExists(file);

        String originalFileName =
                sanitizeOriginalFileName(file.getOriginalFilename());

        String extension = extractExtension(originalFileName);
        FileUploadPolicy policy = FileUploadPolicy.from(assetType);

        validateExtension(policy, extension);
        validateContentType(policy, extension, file.getContentType());
    }

    private void validateFileExists(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FilePolicyViolationException(
                    "FILE_EMPTY",
                    "업로드할 파일이 비어 있습니다."
            );
        }
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new FilePolicyViolationException(
                    "FILE_NAME_MISSING",
                    "원본 파일명이 존재하지 않습니다."
            );
        }

        if (originalFileName.indexOf('\0') >= 0) {
            throw new FilePolicyViolationException(
                    "FILE_NAME_INVALID",
                    "파일명에 허용되지 않은 문자가 포함되어 있습니다."
            );
        }

        String normalizedFileName =
                originalFileName.replace('\\', '/');

        int lastSeparatorIndex =
                normalizedFileName.lastIndexOf('/');

        String sanitizedFileName =
                normalizedFileName.substring(lastSeparatorIndex + 1);

        if (!StringUtils.hasText(sanitizedFileName)) {
            throw new FilePolicyViolationException(
                    "FILE_NAME_INVALID",
                    "유효하지 않은 파일명입니다."
            );
        }

        return sanitizedFileName;
    }

    private String extractExtension(String fileName) {
        String extension =
                StringUtils.getFilenameExtension(fileName);

        if (!StringUtils.hasText(extension)) {
            throw new FilePolicyViolationException(
                    "FILE_EXTENSION_MISSING",
                    "확장자가 없는 파일은 업로드할 수 없습니다."
            );
        }

        return extension.trim().toLowerCase(Locale.ROOT);
    }

    private void validateExtension(
            FileUploadPolicy policy,
            String extension
    ) {
        if (!policy.supportsExtension(extension)) {
            throw new FilePolicyViolationException(
                    "FILE_EXTENSION_NOT_ALLOWED",
                    "허용되지 않은 파일 확장자입니다. 허용 확장자: "
                            + policy.getAllowedExtensions()
            );
        }
    }

    private void validateContentType(
            FileUploadPolicy policy,
            String extension,
            String contentType
    ) {
        if (!StringUtils.hasText(contentType)) {
            return;
        }

        String normalizedContentType =
                normalizeContentType(contentType);

        /*
         * 일부 브라우저와 업로드 클라이언트는 정확한 MIME Type 대신
         * application/octet-stream을 보낼 수 있다.
         *
         * 현재 프로토타입에서는 허용하지만,
         * 이후 Apache Tika 또는 파일 Signature 검사로 보완한다.
         */
        if (MediaType.APPLICATION_OCTET_STREAM_VALUE
                .equals(normalizedContentType)) {
            return;
        }

        if (!policy.supportsContentType(
                extension,
                normalizedContentType
        )) {
            throw new FilePolicyViolationException(
                    "FILE_CONTENT_TYPE_NOT_ALLOWED",
                    "파일 확장자와 Content-Type이 일치하지 않습니다. "
                            + "extension=" + extension
                            + ", contentType=" + normalizedContentType
            );
        }
    }

    private String normalizeContentType(String contentType) {
        String value =
                contentType.trim().toLowerCase(Locale.ROOT);

        int parameterIndex = value.indexOf(';');

        if (parameterIndex >= 0) {
            return value.substring(0, parameterIndex).trim();
        }

        return value;
    }
}