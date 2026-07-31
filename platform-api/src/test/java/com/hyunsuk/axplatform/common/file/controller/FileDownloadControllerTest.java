package com.hyunsuk.axplatform.common.file.controller;

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

import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FileDownloadControllerTest {

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
    void downloadActiveFileMetadataReturnsAttachment()
            throws Exception {
        byte[] pdfBytes = "%PDF-1.4\n%%EOF".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "korean-source.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult uploadResult = mockMvc.perform(
                        multipart("/api/v1/documents")
                                .file(file)
                                .param("title", "Korean source document")
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode uploadResponse = objectMapper.readTree(
                uploadResult.getResponse().getContentAsString()
        );
        long documentId = uploadResponse.get("documentId").asLong();

        MvcResult detailResult = mockMvc.perform(
                        get("/api/v1/documents/{documentId}", documentId)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode detailResponse = objectMapper.readTree(
                detailResult.getResponse().getContentAsString()
        );
        long fileMetadataId = detailResponse
                .get("file")
                .get("fileMetadataId")
                .asLong();

        mockMvc.perform(
                        get(
                                "/api/v1/files/{fileMetadataId}/download",
                                fileMetadataId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Type",
                        containsString("application/pdf")
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        containsString("attachment")
                ))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void downloadMissingFileMetadataReturnsNotFound()
            throws Exception {
        mockMvc.perform(
                        get("/api/v1/files/{fileMetadataId}/download", 999_999L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode")
                        .value("FILE_METADATA_NOT_FOUND"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/files/999999/download"));
    }
}
