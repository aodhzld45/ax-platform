package com.hyunsuk.axplatform.system.service;

import com.hyunsuk.axplatform.system.dto.AiTestRequest;
import com.hyunsuk.axplatform.system.dto.AiTestResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@AllArgsConstructor
public class AiService {
    private final RestClient aiRestClient;

    public AiTestResponse test(AiTestRequest request) {
        System.out.println("Python 전송 DTO = " + request);

        return aiRestClient.post()
                .uri("/api/v1/ai/test")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiTestResponse.class);
    }
}
