package com.hyunsuk.axplatform.aijob.client;

import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonRequest;
import com.hyunsuk.axplatform.aijob.client.dto.AiJobPythonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AiJobPythonClient {

    private final RestClient aiRestClient;

    @Value("${ai.api.ingestion-job-path}")
    private String ingestionJobPath;

    public AiJobPythonResponse requestProcessing(
            AiJobPythonRequest request
    ) {
        return aiRestClient.post()
                .uri(ingestionJobPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiJobPythonResponse.class);
    }
}
