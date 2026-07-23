package com.hyunsuk.axplatform.museum.service;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import com.hyunsuk.axplatform.museum.dto.MuseumManualListResponse;
import com.hyunsuk.axplatform.museum.dto.MuseumManualRequest;
import com.hyunsuk.axplatform.museum.dto.MuseumManualResponse;
import com.hyunsuk.axplatform.museum.entity.MuseumManual;
import com.hyunsuk.axplatform.museum.exception.MuseumManualRegistrationException;
import com.hyunsuk.axplatform.museum.repository.MuseumManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MuseumManualService {

    private final DocumentRepository documentRepository;
    private final MuseumManualRepository museumManualRepository;

    @Transactional(readOnly = true)
    public MuseumManualListResponse findAll(Pageable pageable) {
        Page<MuseumManual> page =
                museumManualRepository.findAllByOrderByIdDesc(pageable);

        List<MuseumManualResponse> items = page.getContent()
                .stream()
                .map(MuseumManualResponse::from)
                .toList();

        return MuseumManualListResponse.of(
                items,
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public MuseumManualResponse findById(Long museumManualId) {
        MuseumManual museumManual = museumManualRepository
                .findWithDocumentById(museumManualId)
                .orElseThrow(() -> new MuseumManualRegistrationException(
                        "MUSEUM_MANUAL_NOT_FOUND",
                        "Museum manual not found. id=" + museumManualId
                ));

        return MuseumManualResponse.from(museumManual);
    }

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
