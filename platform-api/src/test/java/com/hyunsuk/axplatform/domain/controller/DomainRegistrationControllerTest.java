package com.hyunsuk.axplatform.domain.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyunsuk.axplatform.common.file.type.FileAssetType;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DomainRegistrationControllerTest {

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
    void uploadParallelCorpusAndRegisterSignLanguageDataset()
            throws Exception {
        long documentId = uploadDocument(
                "parallel-corpus.csv",
                "text/csv",
                "source,gloss\nhello,GLOSS_HELLO".getBytes(),
                "Parallel corpus",
                FileAssetType.PARALLEL_CORPUS
        );

        mockMvc.perform(
                        post("/api/v1/sign-language-datasets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of(
                                                "documentId", documentId,
                                                "datasetType",
                                                "PARALLEL_CORPUS",
                                                "description",
                                                "Korean-gloss corpus"
                                        )
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.signLanguageDatasetId").exists())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.datasetType")
                        .value("PARALLEL_CORPUS"))
                .andExpect(jsonPath("$.file.assetType")
                        .value("PARALLEL_CORPUS"))
                .andExpect(jsonPath("$.file.originalFileName")
                        .value("parallel-corpus.csv"));
    }

    @Test
    void registerSignLanguageDatasetWithKoreanSourceDocumentReturnsBadRequest()
            throws Exception {
        long documentId = uploadKoreanSourceDocument("Manual for invalid sign");

        mockMvc.perform(
                        post("/api/v1/sign-language-datasets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of(
                                                "documentId", documentId,
                                                "datasetType",
                                                "PARALLEL_CORPUS"
                                        )
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("INVALID_SIGN_LANGUAGE_DATASET_ASSET_TYPE"));
    }

    @Test
    void registerDuplicateSignLanguageDatasetReturnsBadRequest()
            throws Exception {
        long documentId = uploadDocument(
                "gloss-dictionary.json",
                "application/json",
                "{\"GLOSS_HELLO\":\"안녕하세요\"}".getBytes(),
                "Gloss dictionary",
                FileAssetType.GLOSS_DICTIONARY
        );
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "documentId", documentId,
                        "datasetType", "GLOSS_DICTIONARY"
                )
        );

        mockMvc.perform(
                        post("/api/v1/sign-language-datasets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/sign-language-datasets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("SIGN_LANGUAGE_DATASET_ALREADY_REGISTERED"));
    }

    @Test
    void registerMuseumManualReturnsCreated() throws Exception {
        long documentId = uploadKoreanSourceDocument("Museum manual");

        mockMvc.perform(
                        post("/api/v1/museum-manuals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of(
                                                "documentId", documentId,
                                                "museumName",
                                                "National Museum",
                                                "manualCategory",
                                                "Exhibition Guide"
                                        )
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.museumManualId").exists())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.museumName")
                        .value("National Museum"))
                .andExpect(jsonPath("$.file.assetType")
                        .value("KOREAN_SOURCE_DOCUMENT"));
    }

    @Test
    void registerMedicalManualReturnsCreated() throws Exception {
        long documentId = uploadKoreanSourceDocument("Medical manual");

        mockMvc.perform(
                        post("/api/v1/medical-manuals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of(
                                                "documentId", documentId,
                                                "department",
                                                "Emergency",
                                                "manualCategory",
                                                "Reception Guide"
                                        )
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicalManualId").exists())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.department")
                        .value("Emergency"))
                .andExpect(jsonPath("$.file.assetType")
                        .value("KOREAN_SOURCE_DOCUMENT"));
    }

    private long uploadKoreanSourceDocument(String title) throws Exception {
        return uploadDocument(
                "korean-source.pdf",
                "application/pdf",
                "%PDF-1.4\n%%EOF".getBytes(),
                title,
                FileAssetType.KOREAN_SOURCE_DOCUMENT
        );
    }

    private long uploadDocument(
            String originalFileName,
            String contentType,
            byte[] content,
            String title,
            FileAssetType assetType
    ) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalFileName,
                contentType,
                content
        );

        MvcResult result = mockMvc.perform(
                        multipart("/api/v1/documents")
                                .file(file)
                                .param("title", title)
                                .param("assetType", assetType.name())
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
        String accessPath = response.get("accessPath").asText();
        String relativePath = accessPath.replaceFirst("^/files/", "");

        assertThat(Files.exists(uploadRoot.resolve(relativePath)))
                .isTrue();

        return response.get("documentId").asLong();
    }
}
