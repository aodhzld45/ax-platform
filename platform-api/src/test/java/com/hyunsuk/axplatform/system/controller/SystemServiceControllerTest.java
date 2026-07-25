package com.hyunsuk.axplatform.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "ai.api.base-url=http://127.0.0.1:1"
})
@AutoConfigureMockMvc
class SystemServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servicesStatusReturnsOkEvenWhenAiApiIsDown()
            throws Exception {
        mockMvc.perform(get("/api/v1/system/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformApi.status")
                        .value("UP"))
                .andExpect(jsonPath("$.platformApi.latencyMs")
                        .value(0))
                .andExpect(jsonPath("$.aiApi.status")
                        .value("DOWN"))
                .andExpect(jsonPath("$.aiApi.latencyMs").exists())
                .andExpect(jsonPath("$.aiApi.errorCode")
                        .value("AI_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.aiApi.message")
                        .value("Python AI API is unavailable."));
    }
}
