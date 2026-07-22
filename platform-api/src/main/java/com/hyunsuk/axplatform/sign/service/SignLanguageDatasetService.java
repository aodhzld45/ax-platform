package com.hyunsuk.axplatform.sign.service;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import com.hyunsuk.axplatform.sign.dto.SignLanguageDatasetRequest;
import com.hyunsuk.axplatform.sign.dto.SignLanguageDatasetResponse;
import com.hyunsuk.axplatform.sign.entity.SignLanguageDataset;
import com.hyunsuk.axplatform.sign.exception.SignLanguageDatasetRegistrationException;
import com.hyunsuk.axplatform.sign.repository.SignLanguageDatasetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignLanguageDatasetService {

    private static final Set<FileAssetType> ALLOWED_ASSET_TYPES = Set.of(
            FileAssetType.PARALLEL_CORPUS,
            FileAssetType.GLOSS_DICTIONARY
    );

    private final DocumentRepository documentRepository;
    private final SignLanguageDatasetRepository signLanguageDatasetRepository;

    @Transactional
    public SignLanguageDatasetResponse register(
            SignLanguageDatasetRequest request
    ) {
        Document document = documentRepository
                .findWithFileMetadataById(request.getDocumentId())
                .orElseThrow(() -> new DocumentNotFoundException(
                        request.getDocumentId()
                ));

        validateAssetType(document);
        validateDatasetType(request, document);
        validateNotRegistered(document.getId());

        SignLanguageDataset signLanguageDataset =
                SignLanguageDataset.builder()
                        .document(document)
                        .datasetType(request.getDatasetType())
                        .description(request.getDescription())
                        .build();

        SignLanguageDataset savedSignLanguageDataset =
                signLanguageDatasetRepository.save(signLanguageDataset);

        return SignLanguageDatasetResponse.from(savedSignLanguageDataset);
    }

    private void validateAssetType(Document document) {
        FileAssetType assetType =
                document.getFileMetadata().getAssetType();

        if (!ALLOWED_ASSET_TYPES.contains(assetType)) {
            throw new SignLanguageDatasetRegistrationException(
                    "INVALID_SIGN_LANGUAGE_DATASET_ASSET_TYPE",
                    "Document file asset type must be PARALLEL_CORPUS "
                            + "or GLOSS_DICTIONARY."
            );
        }
    }

    private void validateDatasetType(
            SignLanguageDatasetRequest request,
            Document document
    ) {
        if (request.getDatasetType() == null
                || request.getDatasetType().isBlank()) {
            return;
        }

        String assetType = document.getFileMetadata()
                .getAssetType()
                .name();

        if (!assetType.equals(request.getDatasetType())) {
            throw new SignLanguageDatasetRegistrationException(
                    "INVALID_SIGN_LANGUAGE_DATASET_TYPE",
                    "datasetType must match document file asset type."
            );
        }
    }

    private void validateNotRegistered(Long documentId) {
        if (signLanguageDatasetRepository.existsByDocumentId(documentId)) {
            throw new SignLanguageDatasetRegistrationException(
                    "SIGN_LANGUAGE_DATASET_ALREADY_REGISTERED",
                    "Sign language dataset is already registered. documentId="
                            + documentId
            );
        }
    }
}
