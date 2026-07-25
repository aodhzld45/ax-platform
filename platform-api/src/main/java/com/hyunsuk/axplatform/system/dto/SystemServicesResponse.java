package com.hyunsuk.axplatform.system.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemServicesResponse {

    private final ServiceStatusResponse platformApi;
    private final ServiceStatusResponse aiApi;

    public static SystemServicesResponse of(
            ServiceStatusResponse platformApi,
            ServiceStatusResponse aiApi
    ) {
        return SystemServicesResponse.builder()
                .platformApi(platformApi)
                .aiApi(aiApi)
                .build();
    }
}
