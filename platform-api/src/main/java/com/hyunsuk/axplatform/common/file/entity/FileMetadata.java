package com.hyunsuk.axplatform.common.file.entity;

import com.hyunsuk.axplatform.common.entity.BaseTimeEntity;
import com.hyunsuk.axplatform.common.file.dto.StoredFileInfo;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "file_metadata")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMetadata extends BaseTimeEntity {

    /** 파일 메타데이터 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 파일 자산 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 50)
    private FileAssetType assetType;

    /** 파일 보관 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FileMetadataStatus status;

    /** 사용자가 업로드한 원본 파일명 */
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    /** UUID 기반 실제 저장 파일명 */
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    /** 소문자로 정규화된 확장자 */
    @Column(name = "extension", nullable = false, length = 30)
    private String extension;

    /** MIME Type */
    @Column(name = "content_type", length = 150)
    private String contentType;

    /** Byte 단위 파일 크기 */
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    /** 서버 내부 상대 저장 경로 */
    @Column(name = "storage_relative_path", nullable = false, length = 1000)
    private String storageRelativePath;

    /** /files/** HTTP 접근 경로 */
    @Column(name = "access_path", nullable = false, length = 1000)
    private String accessPath;

    /** 파일 무결성 및 중복 검사 값 */
    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Builder
    private FileMetadata(
            FileAssetType assetType,
            FileMetadataStatus status,
            String originalFileName,
            String storedFileName,
            String extension,
            String contentType,
            long fileSize,
            String storageRelativePath,
            String accessPath,
            String checksumSha256
    ) {
        this.assetType = assetType;
        this.status = status == null
                ? FileMetadataStatus.ACTIVE
                : status;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.extension = extension;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storageRelativePath = storageRelativePath;
        this.accessPath = accessPath;
        this.checksumSha256 = checksumSha256;
    }

    public static FileMetadata create(
            FileAssetType assetType,
            StoredFileInfo storedFileInfo
    ) {
        return FileMetadata.builder()
                .assetType(assetType)
                .status(FileMetadataStatus.ACTIVE)
                .originalFileName(storedFileInfo.getOriginalFilename())
                .storedFileName(storedFileInfo.getStoredFilename())
                .extension(storedFileInfo.getExtension())
                .contentType(storedFileInfo.getContentType())
                .fileSize(storedFileInfo.getFileSize())
                .storageRelativePath(storedFileInfo.getStorageRelativePath())
                .accessPath(storedFileInfo.getAccessPath())
                .build();
    }

    public void markDeleted() {
        this.status = FileMetadataStatus.DELETED;
    }

    public void markFailed() {
        this.status = FileMetadataStatus.FAILED;
    }
}
