package com.hyunsuk.axplatform.common.file;

import com.hyunsuk.axplatform.common.file.dto.StoredFileInfo;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.common.file.type.JobFileStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileUtil {

    private static final String PUBLIC_FILE_PREFIX = "/files/";

    private final FileStorageProperties fileStorageProperties;

    /**
     * 일반 관리 자산 저장
     *
     * 예:
     * document/korean-source/{resourceKey}/{version}
     * motion/sign/{resourceKey}/{version}
     * avatar/model/{resourceKey}/{version}
     */
    public StoredFileInfo saveAsset(
            FileAssetType assetType,
            String resourceKey,
            String version,
            MultipartFile file
    ) {
        validateMultipartFile(file);

        String extension = getExtension(
                file.getOriginalFilename()
        );

        validateExtension(assetType, extension);

        Path directory = resolveAssetPath(
                assetType,
                resourceKey,
                version
        );

        try {
            return saveInputStream(
                    directory,
                    file.getOriginalFilename(),
                    extension,
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "업로드 파일을 읽을 수 없습니다.",
                    e
            );
        }
    }

    /**
     * AI Job 관련 업로드 파일 저장
     *
     * 예:
     * job/{jobId}/input
     */
    public StoredFileInfo saveJobFile(
            String jobId,
            JobFileStage stage,
            MultipartFile file
    ) {
        validateMultipartFile(file);

        String extension = getExtension(
                file.getOriginalFilename()
        );

        Path directory = resolveJobPath(
                jobId,
                stage
        );

        try {
            return saveInputStream(
                    directory,
                    file.getOriginalFilename(),
                    extension,
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "AI 작업 파일을 읽을 수 없습니다.",
                    e
            );
        }
    }

    /**
     * Java 또는 Python 연동 결과를 byte[]로 저장
     *
     * 예:
     * gloss-sequence.json
     * sign-video.mp4
     * thumbnail.png
     */
    public StoredFileInfo saveBytes(
            Path directory,
            String originalFilename,
            String contentType,
            byte[] bytes
    ) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException(
                    "저장할 데이터가 없습니다."
            );
        }

        String extension = getExtension(originalFilename);

        return saveInputStream(
                directory,
                originalFilename,
                extension,
                contentType,
                bytes.length,
                new ByteArrayInputStream(bytes)
        );
    }

    /**
     * 일반 자산 저장 경로 계산
     */
    public Path resolveAssetPath(
            FileAssetType assetType,
            String resourceKey,
            String version
    ) {
        if (assetType == null) {
            throw new IllegalArgumentException(
                    "파일 자산 타입이 없습니다."
            );
        }

        validatePathSegment(resourceKey, "resourceKey");
        validatePathSegment(version, "version");

        Path uploadRoot = getUploadRoot();

        Path resolvedPath = uploadRoot
                .resolve(assetType.getBaseDirectory())
                .resolve(resourceKey)
                .resolve(version)
                .toAbsolutePath()
                .normalize();

        validateChildPath(uploadRoot, resolvedPath);

        return resolvedPath;
    }

    /**
     * AI Job 저장 경로 계산
     */
    public Path resolveJobPath(
            String jobId,
            JobFileStage stage
    ) {
        validatePathSegment(jobId, "jobId");

        if (stage == null) {
            throw new IllegalArgumentException(
                    "AI 작업 파일 단계가 없습니다."
            );
        }

        Path uploadRoot = getUploadRoot();

        Path resolvedPath = uploadRoot
                .resolve("job")
                .resolve(jobId)
                .resolve(stage.getDirectory())
                .toAbsolutePath()
                .normalize();

        validateChildPath(uploadRoot, resolvedPath);

        return resolvedPath;
    }

    /**
     * /files/... 경로를 서버 절대 경로로 변환
     */
    public Path resolveAbsolutePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            throw new IllegalArgumentException(
                    "파일 경로가 유효하지 않습니다."
            );
        }

        String relativePath = storedPath.startsWith(PUBLIC_FILE_PREFIX)
                ? storedPath.substring(PUBLIC_FILE_PREFIX.length())
                : storedPath;

        Path uploadRoot = getUploadRoot();

        Path absolutePath = uploadRoot
                .resolve(relativePath)
                .toAbsolutePath()
                .normalize();

        validateChildPath(uploadRoot, absolutePath);

        return absolutePath;
    }

    public String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "";
        }

        return Paths.get(filePath)
                .getFileName()
                .toString();
    }

    public String getExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex <= 0
                || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename
                .substring(lastDotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private StoredFileInfo saveInputStream(
            Path directory,
            String originalFilename,
            String extension,
            String contentType,
            long fileSize,
            InputStream inputStream
    ) {
        if (directory == null) {
            throw new IllegalArgumentException(
                    "저장 경로가 없습니다."
            );
        }

        Path uploadRoot = getUploadRoot();
        Path normalizedDirectory = directory
                .toAbsolutePath()
                .normalize();

        validateChildPath(uploadRoot, normalizedDirectory);

        try (InputStream source = inputStream) {
            Files.createDirectories(normalizedDirectory);

            String storedFilename =
                    createStoredFilename(extension);

            Path fullPath = normalizedDirectory
                    .resolve(storedFilename)
                    .normalize();

            validateChildPath(normalizedDirectory, fullPath);

            Files.copy(
                    source,
                    fullPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String storageRelativePath = uploadRoot
                    .relativize(fullPath)
                    .toString()
                    .replace("\\", "/");

            String accessPath = PUBLIC_FILE_PREFIX
                    + storageRelativePath;

            return StoredFileInfo.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .storedPath(accessPath)
                    .storageRelativePath(storageRelativePath)
                    .accessPath(accessPath)
                    .extension(extension)
                    .contentType(contentType)
                    .fileSize(fileSize)
                    .build();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "파일 저장에 실패했습니다.",
                    e
            );
        }
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "업로드할 파일이 없습니다."
            );
        }

        if (file.getOriginalFilename() == null
                || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException(
                    "원본 파일명이 없습니다."
            );
        }
    }

    private void validateExtension(
            FileAssetType assetType,
            String extension
    ) {
        if (!assetType.supports(extension)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파일 형식입니다. "
                            + "assetType=" + assetType
                            + ", extension=" + extension
            );
        }
    }

    /**
     * 사용자가 보낸 값을 경로에 그대로 넣지 못하도록 제한
     */
    private void validatePathSegment(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " 값이 없습니다."
            );
        }

        if (!value.matches("^[a-zA-Z0-9_-]{1,100}$")) {
            throw new IllegalArgumentException(
                    fieldName
                            + " 형식이 올바르지 않습니다. "
                            + "영문, 숫자, _, -만 사용할 수 있습니다."
            );
        }
    }

    private Path getUploadRoot() {
        String uploadPath =
                fileStorageProperties.getUploadPath();

        if (uploadPath == null || uploadPath.isBlank()) {
            throw new IllegalStateException(
                    "file.upload-path 설정값이 없습니다."
            );
        }

        return Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize();
    }

    private String createStoredFilename(String extension) {
        String uuid = UUID.randomUUID().toString();

        if (extension == null || extension.isBlank()) {
            return uuid;
        }

        return uuid + "." + extension;
    }

    private void validateChildPath(
            Path parentPath,
            Path childPath
    ) {
        if (!childPath.startsWith(parentPath)) {
            throw new IllegalArgumentException(
                    "업로드 루트 외부의 경로는 사용할 수 없습니다."
            );
        }
    }
}
