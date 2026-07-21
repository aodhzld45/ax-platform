package com.hyunsuk.axplatform.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

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
                                .param("title", "국문 수어 변환 원천 문서")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.resourceKey").exists())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.title")
                        .value("국문 수어 변환 원천 문서"))
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

        assertThat(Files.exists(uploadRoot.resolve(relativePath)))
                .isTrue();

        mockMvc.perform(get(accessPath))
                .andExpect(status().isOk())
                .andExpect(content().bytes(pdfBytes));
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
                                .param("title", "잘못된 파일")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode")
                        .value("FILE_EXTENSION_NOT_ALLOWED"));

        try (var paths = Files.walk(uploadRoot)) {
            assertThat(paths.filter(Files::isRegularFile))
                    .isEmpty();
        }
    }
}
