package com.hyunsuk.axplatform.common.file.service;

import com.hyunsuk.axplatform.common.file.FileUtil;
import com.hyunsuk.axplatform.common.file.dto.FileDownloadResource;
import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.entity.FileMetadataStatus;
import com.hyunsuk.axplatform.common.file.exception.FileMetadataNotFoundException;
import com.hyunsuk.axplatform.common.file.exception.StoredFileNotFoundException;
import com.hyunsuk.axplatform.common.file.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private static final String DEFAULT_CONTENT_TYPE =
            "application/octet-stream";

    private final FileMetadataRepository fileMetadataRepository;
    private final FileUtil fileUtil;

    @Transactional(readOnly = true)
    public FileDownloadResource findDownloadResource(
            Long fileMetadataId
    ) {
        FileMetadata fileMetadata = fileMetadataRepository
                .findByIdAndStatus(
                        fileMetadataId,
                        FileMetadataStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new FileMetadataNotFoundException(fileMetadataId));

        Path absolutePath = fileUtil.resolveAbsolutePath(
                fileMetadata.getStorageRelativePath()
        );

        if (!Files.exists(absolutePath)
                || !Files.isRegularFile(absolutePath)) {
            throw new StoredFileNotFoundException(fileMetadataId);
        }

        return FileDownloadResource.builder()
                .resource(new FileSystemResource(absolutePath))
                .originalFileName(fileMetadata.getOriginalFileName())
                .contentType(resolveContentType(fileMetadata))
                .contentLength(resolveContentLength(
                        fileMetadataId,
                        absolutePath
                ))
                .build();
    }

    private String resolveContentType(FileMetadata fileMetadata) {
        if (fileMetadata.getContentType() == null
                || fileMetadata.getContentType().isBlank()) {
            return DEFAULT_CONTENT_TYPE;
        }

        return fileMetadata.getContentType();
    }

    private long resolveContentLength(
            Long fileMetadataId,
            Path absolutePath
    ) {
        try {
            return Files.size(absolutePath);
        } catch (IOException exception) {
            throw new StoredFileNotFoundException(fileMetadataId);
        }
    }
}
