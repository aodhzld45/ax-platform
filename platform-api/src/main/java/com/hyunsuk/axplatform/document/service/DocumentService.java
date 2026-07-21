package com.hyunsuk.axplatform.document.service;

import com.hyunsuk.axplatform.common.file.FileUtil;
import com.hyunsuk.axplatform.common.file.dto.StoredFileInfo;
import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.repository.FileMetadataRepository;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.common.file.validator.FileUploadValidator;
import com.hyunsuk.axplatform.document.dto.DocumentUploadRequest;
import com.hyunsuk.axplatform.document.dto.DocumentUploadResponse;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final FileAssetType SOURCE_DOCUMENT_TYPE =
            FileAssetType.KOREAN_SOURCE_DOCUMENT;
    private static final int INITIAL_VERSION = 1;

    private final FileUploadValidator fileUploadValidator;
    private final FileUtil fileUtil;
    private final FileMetadataRepository fileMetadataRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public DocumentUploadResponse upload(DocumentUploadRequest request) {
        fileUploadValidator.validate(
                request.getFile(),
                SOURCE_DOCUMENT_TYPE
        );

        String resourceKey = UUID.randomUUID().toString();
        Integer version = INITIAL_VERSION;
        StoredFileInfo storedFileInfo = null;

        try {
            storedFileInfo = fileUtil.saveAsset(
                    SOURCE_DOCUMENT_TYPE,
                    resourceKey,
                    String.valueOf(version),
                    request.getFile()
            );

            FileMetadata fileMetadata = FileMetadata.create(
                    SOURCE_DOCUMENT_TYPE,
                    storedFileInfo
            );
            FileMetadata savedFileMetadata =
                    fileMetadataRepository.save(fileMetadata);

            Document document = Document.builder()
                    .resourceKey(resourceKey)
                    .version(version)
                    .title(request.getTitle())
                    .fileMetadata(savedFileMetadata)
                    .build();
            Document savedDocument =
                    documentRepository.save(document);

            return toResponse(
                    savedDocument,
                    storedFileInfo
            );
        } catch (RuntimeException e) {
            deleteStoredFileIfNecessary(
                    storedFileInfo,
                    e
            );
            throw e;
        }
    }

    private DocumentUploadResponse toResponse(
            Document document,
            StoredFileInfo storedFileInfo
    ) {
        return DocumentUploadResponse.builder()
                .documentId(document.getId())
                .resourceKey(document.getResourceKey())
                .version(document.getVersion())
                .title(document.getTitle())
                .originalFileName(storedFileInfo.getOriginalFilename())
                .accessPath(storedFileInfo.getAccessPath())
                .documentStatus(document.getDocumentStatus())
                .indexStatus(document.getIndexStatus())
                .build();
    }

    private void deleteStoredFileIfNecessary(
            StoredFileInfo storedFileInfo,
            RuntimeException originalException
    ) {
        if (storedFileInfo == null) {
            return;
        }

        try {
            fileUtil.deleteByAccessPath(storedFileInfo.getAccessPath());
        } catch (RuntimeException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }
}
