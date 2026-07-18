package com.hyunsuk.axplatform.common.file.repository;

import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.entity.FileMetadataStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository
        extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByAccessPath(
            String accessPath
    );

    Optional<FileMetadata> findByIdAndStatus(
            Long id,
            FileMetadataStatus status
    );

    boolean existsByStorageRelativePath(
            String storageRelativePath
    );
}
