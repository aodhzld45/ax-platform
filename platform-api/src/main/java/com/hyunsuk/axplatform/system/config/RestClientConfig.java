package com.hyunsuk.axplatform.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient aiRestClient(
            RestClient.Builder builder,
            @Value("${ai.api.base-url}") String baseUrl
    ) {
        System.out.println("AI API base URL = " + baseUrl);

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(30_000);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {

                    System.out.println("AI 요청 URI = " + request.getURI());
                    System.out.println("AI 요청 Method = " + request.getMethod());
                    System.out.println("AI 요청 Headers = " + request.getHeaders());
                    System.out.println("AI 요청 Body 길이 = " + body.length);
                    System.out.println(
                            "AI 요청 Body = "
                                    + new String(body, StandardCharsets.UTF_8)
                    );

                    return execution.execute(request, body);
                })
                .build();
    }
}