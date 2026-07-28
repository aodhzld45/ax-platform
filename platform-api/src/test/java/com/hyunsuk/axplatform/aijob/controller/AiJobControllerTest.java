package com.hyunsuk.axplatform.aijob.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunsuk.axplatform.aijob.client.AiJobPythonClient;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonRequest;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonResponse;
import com.hyunsuk.axplatform.aijob.entity.AiJobFile;
import com.hyunsuk.axplatform.aijob.repository.AiJobFileRepository;
import com.hyunsuk.axplatform.common.file.entity.FileMetadata;
import com.hyunsuk.axplatform.common.file.repository.FileMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.ResourceAccessException;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiJobControllerTest {

    @TempDir
    static Path uploadRoot;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiJobFileRepository aiJobFileRepository;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @MockitoBean
    private AiJobPythonClient aiJobPythonClient;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "file.upload-path",
                () -> uploadRoot.toString()
        );
    }

    @Test
    void createPendingJobAndFindByJobKeyAndDocument()
            throws Exception {
        when(aiJobPythonClient.requestProcessing(any()))
                .thenAnswer(invocation -> {
                    AiJobPythonRequest request = invocation.getArgument(0);

                    return new AiJobPythonResponse(
                            request.getJobId(),
                            true,
                            "PENDING",
                            "accepted"
                    );
                });

        long documentId = uploadKoreanSourceDocument();

        MvcResult result = mockMvc.perform(
                        post(
                                "/api/v1/documents/{documentId}/ai-jobs",
                                documentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "jobType": "KOREAN_TO_GLOSS"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        startsWith("/api/v1/ai-jobs/JOB_")
                ))
                .andExpect(jsonPath("$.aiJobId").exists())
                .andExpect(jsonPath("$.jobKey").exists())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.documentTitle")
                        .value("Korean source document"))
                .andExpect(jsonPath("$.jobType")
                        .value("KOREAN_TO_GLOSS"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.currentStage")
                        .value("TEXT_EXTRACTION"))
                .andExpect(jsonPath("$.progress").value(10))
                .andExpect(jsonPath("$.retryCount").value(0))
                .andExpect(jsonPath("$.requestedAt").exists())
                .andExpect(jsonPath("$.startedAt").exists())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
        String jobKey = response.get("jobKey").asText();

        mockMvc.perform(get("/api/v1/ai-jobs/{jobKey}", jobKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobKey").value(jobKey))
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        mockMvc.perform(
                        get(
                                "/api/v1/documents/{documentId}/ai-jobs",
                                documentId
                        )
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].jobKey").value(jobKey))
                .andExpect(jsonPath("$.totalCount").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void createJobStoresFailedWhenPythonRequestFails()
            throws Exception {
        when(aiJobPythonClient.requestProcessing(any()))
                .thenThrow(new ResourceAccessException("connection refused"));

        long documentId = uploadKoreanSourceDocument();

        mockMvc.perform(
                        post(
                                "/api/v1/documents/{documentId}/ai-jobs",
                                documentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "jobType": "KOREAN_TO_GLOSS"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.currentStage")
                        .value("FILE_PREPARATION"))
                .andExpect(jsonPath("$.progress").value(0))
                .andExpect(jsonPath("$.errorCode")
                        .value("AI_API_REQUEST_FAILED"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Python AI API processing request failed."))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void handleProcessingCallbackUpdatesStageAndProgress()
            throws Exception {
        String jobKey = createProcessingJob();

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "KOREAN_NORMALIZATION",
                                          "progress": 40,
                                          "message": "국문 정규화 완료"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobKey").value(jobKey))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.currentStage")
                        .value("KOREAN_NORMALIZATION"))
                .andExpect(jsonPath("$.progress").value(40));
    }

    @Test
    void handleCompletedCallbackCompletesJob()
            throws Exception {
        String jobKey = createProcessingJob();

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "COMPLETED",
                                          "stage": "RESULT_FINALIZATION",
                                          "progress": 100,
                                          "resultJson": "{\\"glossCount\\":3}"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.currentStage")
                        .value("RESULT_FINALIZATION"))
                .andExpect(jsonPath("$.progress").value(100))
                .andExpect(jsonPath("$.resultJson")
                        .value("{\"glossCount\":3}"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void callbackStoresResultFilesAndSkipsDuplicateFileRole()
            throws Exception {
        String jobKey = createProcessingJob();

        MvcResult callbackResult = mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "GLOSS_GENERATION",
                                          "progress": 70,
                                          "files": [
                                            {
                                              "role": "GLOSS_SEQUENCE",
                                              "assetType": "JOB_INTERMEDIATE",
                                              "originalFileName": "gloss-sequence.json",
                                              "storedFileName": "gloss-sequence_001.json",
                                              "extension": "json",
                                              "contentType": "application/json",
                                              "fileSize": 128,
                                              "storageRelativePath": "job/JOB_TEST/intermediate/gloss-sequence_001.json",
                                              "accessPath": "/files/job/JOB_TEST/intermediate/gloss-sequence_001.json",
                                              "checksumSha256": "abc123"
                                            }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn();

        long aiJobId = objectMapper.readTree(
                callbackResult.getResponse().getContentAsString()
        ).get("aiJobId").asLong();

        List<AiJobFile> files =
                aiJobFileRepository.findAllByAiJobIdOrderByIdAsc(aiJobId);
        assertThat(files).hasSize(1);
        assertThat(files.get(0).getRole().name())
                .isEqualTo("GLOSS_SEQUENCE");

        FileMetadata fileMetadata = fileMetadataRepository
                .findByAccessPath(
                        "/files/job/JOB_TEST/intermediate/gloss-sequence_001.json"
                )
                .orElseThrow();
        assertThat(fileMetadata.getAssetType().name())
                .isEqualTo("JOB_INTERMEDIATE");
        assertThat(fileMetadata.getStorageRelativePath())
                .isEqualTo(
                        "job/JOB_TEST/intermediate/gloss-sequence_001.json"
                );

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "GLOSS_GENERATION",
                                          "progress": 70,
                                          "files": [
                                            {
                                              "role": "GLOSS_SEQUENCE",
                                              "assetType": "JOB_INTERMEDIATE",
                                              "storageRelativePath": "job/JOB_TEST/intermediate/gloss-sequence_001.json",
                                              "accessPath": "/files/job/JOB_TEST/intermediate/gloss-sequence_001.json"
                                            }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        assertThat(aiJobFileRepository.findAllByAiJobIdOrderByIdAsc(aiJobId))
                .hasSize(1);
    }

    @Test
    void handleFailedCallbackStoresError()
            throws Exception {
        String jobKey = createProcessingJob();

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "FAILED",
                                          "stage": "TEXT_EXTRACTION",
                                          "progress": 20,
                                          "errorCode": "TEXT_EXTRACTION_FAILED",
                                          "errorMessage": "PDF 텍스트 추출 실패"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.currentStage")
                        .value("TEXT_EXTRACTION"))
                .andExpect(jsonPath("$.errorCode")
                        .value("TEXT_EXTRACTION_FAILED"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("PDF 텍스트 추출 실패"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void callbackWithMissingJobReturnsNotFound()
            throws Exception {
        mockMvc.perform(
                        patch(
                                "/api/v1/ai-jobs/{jobKey}/callback",
                                "JOB_UNKNOWN"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "TEXT_EXTRACTION",
                                          "progress": 20
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value("AI_JOB_NOT_FOUND"));
    }

    @Test
    void callbackRejectsBackwardStage()
            throws Exception {
        String jobKey = createProcessingJob();

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "KOREAN_NORMALIZATION",
                                          "progress": 40
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "TEXT_EXTRACTION",
                                          "progress": 50
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode")
                        .value("AI_JOB_STATE_CONFLICT"));
    }

    @Test
    void callbackRejectsLowerProgress()
            throws Exception {
        String jobKey = createProcessingJob();

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "KOREAN_NORMALIZATION",
                                          "progress": 40
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "GLOSS_GENERATION",
                                          "progress": 30
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode")
                        .value("AI_JOB_STATE_CONFLICT"));
    }

    @Test
    void completedJobRejectsProcessingCallback()
            throws Exception {
        String jobKey = createProcessingJob();

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "COMPLETED",
                                          "stage": "RESULT_FINALIZATION",
                                          "progress": 100,
                                          "resultJson": "{}"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/ai-jobs/{jobKey}/callback", jobKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "status": "PROCESSING",
                                          "stage": "RESULT_FINALIZATION",
                                          "progress": 90
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode")
                        .value("AI_JOB_STATE_CONFLICT"));
    }

    @Test
    void createPendingJobWithMissingDocumentReturnsNotFound()
            throws Exception {
        mockMvc.perform(post("/api/v1/documents/{documentId}/ai-jobs", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value("DOCUMENT_NOT_FOUND"));
    }

    @Test
    void findMissingJobReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/ai-jobs/{jobKey}", "JOB_UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value("AI_JOB_NOT_FOUND"));
    }

    private long uploadKoreanSourceDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "korean-source.pdf",
                "application/pdf",
                "%PDF-1.4\n%%EOF".getBytes()
        );

        MvcResult result = mockMvc.perform(
                        multipart("/api/v1/documents")
                                .file(file)
                                .param("title", "Korean source document")
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response.get("documentId").asLong();
    }

    private String createProcessingJob() throws Exception {
        when(aiJobPythonClient.requestProcessing(any()))
                .thenAnswer(invocation -> {
                    AiJobPythonRequest request = invocation.getArgument(0);

                    return new AiJobPythonResponse(
                            request.getJobId(),
                            true,
                            "PENDING",
                            "accepted"
                    );
                });

        long documentId = uploadKoreanSourceDocument();

        MvcResult result = mockMvc.perform(
                        post(
                                "/api/v1/documents/{documentId}/ai-jobs",
                                documentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "jobType": "KOREAN_TO_GLOSS"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response.get("jobKey").asText();
    }
}
