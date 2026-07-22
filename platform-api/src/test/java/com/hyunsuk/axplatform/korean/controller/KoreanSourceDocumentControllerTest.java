package com.hyunsuk.axplatform.korean.controller;

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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class KoreanSourceDocumentControllerTest {

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
    void registerKoreanSourceDocumentReturnsCreated()
            throws Exception {
        long documentId = uploadKoreanSourceDocument();

        mockMvc.perform(
                        post("/api/v1/korean-source-documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of(
                                                "documentId", documentId,
                                                "sourceDomain",
                                                "PUBLIC_SERVICE",
                                                "description",
                                                "Public service source text"
                                        )
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        org.hamcrest.Matchers.startsWith(
                                "/api/v1/korean-source-documents/"
                        )
                ))
                .andExpect(jsonPath("$.koreanSourceDocumentId").exists())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.sourceDomain")
                        .value("PUBLIC_SERVICE"))
                .andExpect(jsonPath("$.description")
                        .value("Public service source text"))
                .andExpect(jsonPath("$.file.assetType")
                        .value("KOREAN_SOURCE_DOCUMENT"))
                .andExpect(jsonPath("$.file.originalFileName")
                        .value("korean-source.pdf"));
    }

    @Test
    void registerDuplicateDocumentReturnsBadRequest()
            throws Exception {
        long documentId = uploadKoreanSourceDocument();
        String requestBody = objectMapper.writeValueAsString(
                Map.of(
                        "documentId", documentId,
                        "sourceDomain", "PUBLIC_SERVICE"
                )
        );

        mockMvc.perform(
                        post("/api/v1/korean-source-documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/korean-source-documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("KOREAN_SOURCE_DOCUMENT_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/korean-source-documents"));
    }

    @Test
    void registerMissingDocumentReturnsNotFound() throws Exception {
        mockMvc.perform(
                        post("/api/v1/korean-source-documents")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of("documentId", 999_999L)
                                ))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value("DOCUMENT_NOT_FOUND"));
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
