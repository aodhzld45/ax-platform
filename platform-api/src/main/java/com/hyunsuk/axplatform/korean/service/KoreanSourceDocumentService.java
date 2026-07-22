package com.hyunsuk.axplatform.korean.service;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentRequest;
import com.hyunsuk.axplatform.korean.dto.KoreanSourceDocumentResponse;
import com.hyunsuk.axplatform.korean.entity.KoreanSourceDocument;
import com.hyunsuk.axplatform.korean.exception.KoreanSourceDocumentRegistrationException;
import com.hyunsuk.axplatform.korean.repository.KoreanSourceDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KoreanSourceDocumentService {

    private final DocumentRepository documentRepository;
    private final KoreanSourceDocumentRepository
            koreanSourceDocumentRepository;

    @Transactional
    public KoreanSourceDocumentResponse register(
            KoreanSourceDocumentRequest request
    ) {
        Document document = documentRepository
                .findWithFileMetadataById(request.getDocumentId())
                .orElseThrow(() -> new DocumentNotFoundException(
                        request.getDocumentId()
                ));

        validateAssetType(document);
        validateNotRegistered(document.getId());

        KoreanSourceDocument koreanSourceDocument =
                KoreanSourceDocument.builder()
                        .document(document)
                        .sourceDomain(request.getSourceDomain())
                        .description(request.getDescription())
                        .build();

        KoreanSourceDocument savedKoreanSourceDocument =
                koreanSourceDocumentRepository.save(koreanSourceDocument);

        return KoreanSourceDocumentResponse.from(savedKoreanSourceDocument);
    }

    private void validateAssetType(Document document) {
        FileAssetType assetType =
                document.getFileMetadata().getAssetType();

        if (assetType != FileAssetType.KOREAN_SOURCE_DOCUMENT) {
            throw new KoreanSourceDocumentRegistrationException(
                    "INVALID_DOCUMENT_ASSET_TYPE",
                    "Document file asset type must be KOREAN_SOURCE_DOCUMENT."
            );
        }
    }

    private void validateNotRegistered(Long documentId) {
        if (koreanSourceDocumentRepository.existsByDocumentId(documentId)) {
            throw new KoreanSourceDocumentRegistrationException(
                    "KOREAN_SOURCE_DOCUMENT_ALREADY_REGISTERED",
                    "Korean source document is already registered. documentId="
                            + documentId
            );
        }
    }
}
