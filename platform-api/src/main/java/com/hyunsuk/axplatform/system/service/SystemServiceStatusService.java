package com.hyunsuk.axplatform.system.service;

import com.hyunsuk.axplatform.system.dto.ServiceStatusResponse;
import com.hyunsuk.axplatform.system.dto.SystemServicesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class SystemServiceStatusService {

    private final RestClient aiRestClient;

    public SystemServicesResponse getServicesStatus() {
        return SystemServicesResponse.of(
                ServiceStatusResponse.up(0L),
                getAiApiStatus()
        );
    }

    private ServiceStatusResponse getAiApiStatus() {
        long startedAt = System.nanoTime();

        try {
            aiRestClient.get()
                    .uri("/api/v1/health")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();

            return ServiceStatusResponse.up(
                    elapsedMillis(startedAt)
            );
        } catch (RestClientException exception) {
            return ServiceStatusResponse.down(
                    elapsedMillis(startedAt),
                    "AI_API_UNAVAILABLE",
                    "Python AI API is unavailable."
            );
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
