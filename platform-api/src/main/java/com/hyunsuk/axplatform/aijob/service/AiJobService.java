package com.hyunsuk.axplatform.aijob.service;

import com.hyunsuk.axplatform.aijob.client.AiJobPythonClient;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonRequest;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonResponse;
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
import org.springframework.web.client.RestClientException;

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
    private final AiJobPythonClient aiJobPythonClient;

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

        AiJob savedAiJob = aiJobRepository.save(aiJob);
        requestPythonProcessing(savedAiJob);

        return AiJobResponse.from(savedAiJob);
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

    private void requestPythonProcessing(AiJob aiJob) {
        try {
            AiJobPythonResponse response =
                    aiJobPythonClient.requestProcessing(
                            AiJobPythonRequest.from(aiJob)
                    );

            if (response == null || !response.isAccepted()) {
                aiJob.fail(
                        "AI_API_REQUEST_REJECTED",
                        "Python AI API rejected the processing request."
                );
                return;
            }

            aiJob.start();
        } catch (RestClientException exception) {
            aiJob.fail(
                    "AI_API_REQUEST_FAILED",
                    "Python AI API processing request failed."
            );
        }
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
