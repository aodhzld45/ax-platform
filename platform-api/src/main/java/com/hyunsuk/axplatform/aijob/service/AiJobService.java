package com.hyunsuk.axplatform.aijob.service;

import com.hyunsuk.axplatform.aijob.dto.AiJobCreateRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobListResponse;
import com.hyunsuk.axplatform.aijob.dto.AiJobResponse;
import com.hyunsuk.axplatform.aijob.entity.AiJob;
import com.hyunsuk.axplatform.aijob.entity.AiJobType;
import com.hyunsuk.axplatform.aijob.exception.AiJobNotFoundException;
import com.hyunsuk.axplatform.aijob.repository.AiJobRepository;
import com.hyunsuk.axplatform.document.entity.Document;
import com.hyunsuk.axplatform.document.exception.DocumentNotFoundException;
import com.hyunsuk.axplatform.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiJobService {

    private static final DateTimeFormatter JOB_DATE_FORMATTER =
            DateTimeFormatter.BASIC_ISO_DATE;

    private final DocumentRepository documentRepository;
    private final AiJobRepository aiJobRepository;

    @Transactional
    public AiJobResponse createPendingJob(
            Long documentId,
            AiJobCreateRequest request
    ) {
        Document document = documentRepository
                .findWithFileMetadataById(documentId)
                .orElseThrow(() ->
                        new DocumentNotFoundException(documentId));

        AiJob aiJob = AiJob.createPending(
                createJobKey(),
                document,
                resolveJobType(request)
        );

        return AiJobResponse.from(aiJobRepository.save(aiJob));
    }

    @Transactional(readOnly = true)
    public AiJobResponse findByJobKey(String jobKey) {
        AiJob aiJob = aiJobRepository.findByJobKey(jobKey)
                .orElseThrow(() -> new AiJobNotFoundException(jobKey));

        return AiJobResponse.from(aiJob);
    }

    @Transactional(readOnly = true)
    public AiJobListResponse findAllByDocumentId(
            Long documentId,
            Pageable pageable
    ) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }

        Page<AiJob> page = aiJobRepository
                .findAllByDocumentIdOrderByIdDesc(
                        documentId,
                        pageable
                );

        List<AiJobResponse> items = page.getContent()
                .stream()
                .map(AiJobResponse::from)
                .toList();

        return AiJobListResponse.of(
                items,
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private AiJobType resolveJobType(AiJobCreateRequest request) {
        if (request == null || request.getJobType() == null) {
            return AiJobType.KOREAN_TO_GLOSS;
        }

        return request.getJobType();
    }

    private String createJobKey() {
        return "JOB_"
                + LocalDate.now().format(JOB_DATE_FORMATTER)
                + "_"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}
