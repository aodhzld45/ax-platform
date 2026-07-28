package com.hyunsuk.axplatform.aijob.service;

import com.hyunsuk.axplatform.aijob.client.AiJobPythonClient;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonRequest;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonResponse;
import com.hyunsuk.axplatform.aijob.dto.AiJobCallbackFileRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobCallbackRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobCreateRequest;
import com.hyunsuk.axplatform.aijob.dto.AiJobListResponse;
import com.hyunsuk.axplatform.aijob.dto.AiJobResponse;
import com.hyunsuk.axplatform.aijob.entity.AiJob;
import com.hyunsuk.axplatform.aijob.entity.AiJobFile;
import com.hyunsuk.axplatform.aijob.entity.AiJobFileRole;
import com.hyunsuk.axplatform.aijob.entity.AiJobStatus;
import com.hyunsuk.axplatform.aijob.entity.AiJobType;
import com.hyunsuk.axplatform.aijob.exception.AiJobNotFoundException;
import com.hyunsuk.axplatform.aijob.repository.AiJobFileRepository;
import com.hyunsuk.axplatform.aijob.repository.AiJobRepository;
import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.repository.FileMetadataRepository;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
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
    private final AiJobFileRepository aiJobFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
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

    @Transactional
    public AiJobResponse handleCallback(
            String jobKey,
            AiJobCallbackRequest request
    ) {
        AiJob aiJob = aiJobRepository.findByJobKey(jobKey)
                .orElseThrow(() -> new AiJobNotFoundException(jobKey));

        validateCallbackRequest(request);

        if (request.getStatus() == AiJobStatus.PROCESSING) {
            aiJob.updateStage(
                    request.getStage(),
                    request.getProgress()
            );
        } else if (request.getStatus() == AiJobStatus.COMPLETED) {
            aiJob.complete(
                    request.getStage(),
                    request.getResultJson()
            );
        } else if (request.getStatus() == AiJobStatus.FAILED) {
            aiJob.fail(
                    request.getStage(),
                    request.getErrorCode(),
                    request.getErrorMessage()
            );
        } else {
            throw new IllegalArgumentException(
                    "지원하지 않는 AiJob Callback 상태입니다. "
                            + request.getStatus()
            );
        }

        saveCallbackFiles(aiJob, request);

        return AiJobResponse.from(aiJob);
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

    private void validateCallbackRequest(AiJobCallbackRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "AiJob Callback 요청은 필수입니다."
            );
        }

        if (request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "AiJob Callback 상태는 필수입니다."
            );
        }

        if (request.getStage() == null) {
            throw new IllegalArgumentException(
                    "AiJob Callback 단계는 필수입니다."
            );
        }

        if (request.getStatus() == AiJobStatus.PROCESSING
                && request.getProgress() == null) {
            throw new IllegalArgumentException(
                    "PROCESSING Callback 진행률은 필수입니다."
            );
        }
    }

    private void saveCallbackFiles(
            AiJob aiJob,
            AiJobCallbackRequest request
    ) {
        if (request.getFiles() == null || request.getFiles().isEmpty()) {
            return;
        }

        for (AiJobCallbackFileRequest fileRequest : request.getFiles()) {
            validateCallbackFileRequest(fileRequest);

            if (aiJobFileRepository.existsByAiJobIdAndStageAndRole(
                    aiJob.getId(),
                    request.getStage(),
                    fileRequest.getRole()
            )) {
                continue;
            }

            FileAssetType assetType = resolveFileAssetType(fileRequest);
            FileMetadata fileMetadata = fileMetadataRepository.save(
                    FileMetadata.createJobFile(
                            assetType,
                            fileRequest.getOriginalFileName(),
                            fileRequest.getStoredFileName(),
                            fileRequest.getExtension(),
                            fileRequest.getContentType(),
                            fileRequest.getFileSize(),
                            fileRequest.getStorageRelativePath(),
                            fileRequest.getAccessPath(),
                            fileRequest.getChecksumSha256()
                    )
            );

            aiJobFileRepository.save(
                    AiJobFile.create(
                            aiJob,
                            request.getStage(),
                            fileRequest.getRole(),
                            fileMetadata
                    )
            );
        }
    }

    private void validateCallbackFileRequest(
            AiJobCallbackFileRequest fileRequest
    ) {
        if (fileRequest == null) {
            throw new IllegalArgumentException(
                    "AiJob Callback 파일 정보는 필수입니다."
            );
        }

        if (fileRequest.getRole() == null) {
            throw new IllegalArgumentException(
                    "AiJob Callback 파일 역할은 필수입니다."
            );
        }

        if (!hasText(fileRequest.getStorageRelativePath())) {
            throw new IllegalArgumentException(
                    "AiJob Callback 파일 상대 저장 경로는 필수입니다."
            );
        }

        if (!hasText(fileRequest.getAccessPath())) {
            throw new IllegalArgumentException(
                    "AiJob Callback 파일 접근 경로는 필수입니다."
            );
        }
    }

    private FileAssetType resolveFileAssetType(
            AiJobCallbackFileRequest fileRequest
    ) {
        if (fileRequest.getAssetType() != null) {
            return fileRequest.getAssetType();
        }

        if (fileRequest.getRole() == AiJobFileRole.SIGN_VIDEO) {
            return FileAssetType.JOB_OUTPUT;
        }

        return FileAssetType.JOB_INTERMEDIATE;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
