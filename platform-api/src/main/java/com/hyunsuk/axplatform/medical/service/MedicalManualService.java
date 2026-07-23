package com.hyunsuk.axplatform.medical.service;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import com.hyunsuk.axplatform.medical.dto.MedicalManualListResponse;
import com.hyunsuk.axplatform.medical.dto.MedicalManualRequest;
import com.hyunsuk.axplatform.medical.dto.MedicalManualResponse;
import com.hyunsuk.axplatform.medical.entity.MedicalManual;
import com.hyunsuk.axplatform.medical.exception.MedicalManualRegistrationException;
import com.hyunsuk.axplatform.medical.repository.MedicalManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalManualService {

    private final DocumentRepository documentRepository;
    private final MedicalManualRepository medicalManualRepository;

    @Transactional(readOnly = true)
    public MedicalManualListResponse findAll(Pageable pageable) {
        Page<MedicalManual> page =
                medicalManualRepository.findAllByOrderByIdDesc(pageable);

        List<MedicalManualResponse> items = page.getContent()
                .stream()
                .map(MedicalManualResponse::from)
                .toList();

        return MedicalManualListResponse.of(
                items,
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public MedicalManualResponse findById(Long medicalManualId) {
        MedicalManual medicalManual = medicalManualRepository
                .findWithDocumentById(medicalManualId)
                .orElseThrow(() -> new MedicalManualRegistrationException(
                        "MEDICAL_MANUAL_NOT_FOUND",
                        "Medical manual not found. id=" + medicalManualId
                ));

        return MedicalManualResponse.from(medicalManual);
    }

    @Transactional
    public MedicalManualResponse register(MedicalManualRequest request) {
        Document document = documentRepository
                .findWithFileMetadataById(request.getDocumentId())
                .orElseThrow(() -> new DocumentNotFoundException(
                        request.getDocumentId()
                ));

        validateAssetType(document);
        validateNotRegistered(document.getId());

        MedicalManual medicalManual = MedicalManual.builder()
                .document(document)
                .department(request.getDepartment())
                .manualCategory(request.getManualCategory())
                .build();

        return MedicalManualResponse.from(
                medicalManualRepository.save(medicalManual)
        );
    }

    private void validateAssetType(Document document) {
        if (document.getFileMetadata().getAssetType()
                != FileAssetType.KOREAN_SOURCE_DOCUMENT) {
            throw new MedicalManualRegistrationException(
                    "INVALID_MEDICAL_MANUAL_ASSET_TYPE",
                    "Document file asset type must be KOREAN_SOURCE_DOCUMENT."
            );
        }
    }

    private void validateNotRegistered(Long documentId) {
        if (medicalManualRepository.existsByDocumentId(documentId)) {
            throw new MedicalManualRegistrationException(
                    "MEDICAL_MANUAL_ALREADY_REGISTERED",
                    "Medical manual is already registered. documentId="
                            + documentId
            );
        }
    }
}
