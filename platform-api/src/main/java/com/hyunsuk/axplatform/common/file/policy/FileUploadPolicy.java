package com.hyunsuk.axplatform.common.file.policy;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum FileUploadPolicy {

    KOREAN_SOURCE_DOCUMENT(
            FileAssetType.KOREAN_SOURCE_DOCUMENT,
            Map.of(
                    "pdf", Set.of(
                            "application/pdf"
                    )
            )
    );

    private final FileAssetType assetType;
    private final Map<String, Set<String>> allowedContentTypesByExtension;
    private final Set<String> allowedExtensions;

    FileUploadPolicy(
            FileAssetType assetType,
            Map<String, Set<String>> allowedContentTypesByExtension
    ) {
        this.assetType = assetType;

        this.allowedContentTypesByExtension =
                allowedContentTypesByExtension.entrySet()
                        .stream()
                        .collect(Collectors.toUnmodifiableMap(
                                entry -> normalizeExtension(entry.getKey()),
                                entry -> entry.getValue()
                                        .stream()
                                        .map(FileUploadPolicy::normalizeContentType)
                                        .collect(Collectors.toUnmodifiableSet())
                        ));

        this.allowedExtensions =
                Set.copyOf(this.allowedContentTypesByExtension.keySet());
    }

    public static FileUploadPolicy from(FileAssetType assetType) {
        for (FileUploadPolicy policy : values()) {
            if (policy.assetType == assetType) {
                return policy;
            }
        }

        throw new IllegalArgumentException(
                "File upload policy is not defined for asset type: " + assetType
        );
    }

    public boolean supportsExtension(String extension) {
        return allowedExtensions.contains(normalizeExtension(extension));
    }

    public boolean supportsContentType(
            String extension,
            String contentType
    ) {
        String normalizedExtension = normalizeExtension(extension);
        String normalizedContentType = normalizeContentType(contentType);

        Set<String> allowedContentTypes =
                allowedContentTypesByExtension.get(normalizedExtension);

        return allowedContentTypes != null
                && allowedContentTypes.contains(normalizedContentType);
    }

    private static String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }

        return extension
                .trim()
                .replaceFirst("^\\.", "")
                .toLowerCase(Locale.ROOT);
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        String value = contentType.trim().toLowerCase(Locale.ROOT);
        int parameterIndex = value.indexOf(';');

        if (parameterIndex >= 0) {
            return value.substring(0, parameterIndex).trim();
        }

        return value;
    }
}
