package com.hyunsuk.axplatform.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {

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
    void uploadPdfReturnsCreatedAndServesFile() throws Exception {
        byte[] pdfBytes = "%PDF-1.4\n%%EOF".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "korean-source.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult result = mockMvc.perform(
                        multipart("/api/v1/documents")
                                .file(file)
                                .param("title", "Korean source document")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.resourceKey").exists())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Korean source document"))
                .andExpect(jsonPath("$.originalFileName")
                        .value("korean-source.pdf"))
                .andExpect(jsonPath("$.accessPath").exists())
                .andExpect(jsonPath("$.documentStatus")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$.indexStatus")
                        .value("NOT_REQUESTED"))
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
        String accessPath = response.get("accessPath").asText();
        String relativePath = accessPath.replaceFirst("^/files/", "");
        long documentId = response.get("documentId").asLong();

        assertThat(Files.exists(uploadRoot.resolve(relativePath)))
                .isTrue();

        mockMvc.perform(get(accessPath))
                .andExpect(status().isOk())
                .andExpect(content().bytes(pdfBytes));

        mockMvc.perform(get("/api/v1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentId").exists())
                .andExpect(jsonPath("$[0].file.accessPath").exists());

        mockMvc.perform(get("/api/v1/documents/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.title")
                        .value("Korean source document"))
                .andExpect(jsonPath("$.file.originalFileName")
                        .value("korean-source.pdf"))
                .andExpect(jsonPath("$.file.accessPath")
                        .value(accessPath));
    }

    @Test
    void uploadNonPdfReturnsBadRequestWithoutSavingFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "korean-source.txt",
                "text/plain",
                "not pdf".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/v1/documents")
                                .file(file)
                                .param("title", "Invalid file")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("FILE_EXTENSION_NOT_ALLOWED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/documents"))
                .andExpect(jsonPath("$.timestamp").exists());

        try (var paths = Files.walk(uploadRoot)) {
            assertThat(paths.filter(Files::isRegularFile))
                    .isEmpty();
        }
    }

    @Test
    void findMissingDocumentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/documents/{documentId}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value("DOCUMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/documents/999999"));
    }
}
