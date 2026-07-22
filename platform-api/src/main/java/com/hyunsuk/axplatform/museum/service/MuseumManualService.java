package com.hyunsuk.axplatform.museum.service;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import com.hyunsuk.axplatform.museum.dto.MuseumManualRequest;
import com.hyunsuk.axplatform.museum.dto.MuseumManualResponse;
import com.hyunsuk.axplatform.museum.entity.MuseumManual;
import com.hyunsuk.axplatform.museum.exception.MuseumManualRegistrationException;
import com.hyunsuk.axplatform.museum.repository.MuseumManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MuseumManualService {

    private final DocumentRepository documentRepository;
    private final MuseumManualRepository museumManualRepository;

    @Transactional
    public MuseumManualResponse register(MuseumManualRequest request) {
        Document document = documentRepository
                .findWithFileMetadataById(request.getDocumentId())
                .orElseThrow(() -> new DocumentNotFoundException(
                        request.getDocumentId()
                ));

        validateAssetType(document);
        validateNotRegistered(document.getId());

        MuseumManual museumManual = MuseumManual.builder()
                .document(document)
                .museumName(request.getMuseumName())
                .manualCategory(request.getManualCategory())
                .build();

        return MuseumManualResponse.from(
                museumManualRepository.save(museumManual)
        );
    }

    private void validateAssetType(Document document) {
        if (document.getFileMetadata().getAssetType()
                != FileAssetType.KOREAN_SOURCE_DOCUMENT) {
            throw new MuseumManualRegistrationException(
                    "INVALID_MUSEUM_MANUAL_ASSET_TYPE",
                    "Document file asset type must be KOREAN_SOURCE_DOCUMENT."
            );
        }
    }

    private void validateNotRegistered(Long documentId) {
        if (museumManualRepository.existsByDocumentId(documentId)) {
            throw new MuseumManualRegistrationException(
                    "MUSEUM_MANUAL_ALREADY_REGISTERED",
                    "Museum manual is already registered. documentId="
                            + documentId
            );
        }
    }
}
