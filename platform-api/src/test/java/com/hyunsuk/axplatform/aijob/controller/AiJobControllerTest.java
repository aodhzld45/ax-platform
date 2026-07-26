package com.hyunsuk.axplatform.aijob.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.currentStage")
                        .value("FILE_PREPARATION"))
                .andExpect(jsonPath("$.progress").value(0))
                .andExpect(jsonPath("$.retryCount").value(0))
                .andExpect(jsonPath("$.requestedAt").exists())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
        String jobKey = response.get("jobKey").asText();

        mockMvc.perform(get("/api/v1/ai-jobs/{jobKey}", jobKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobKey").value(jobKey))
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.status").value("PENDING"));

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
}
