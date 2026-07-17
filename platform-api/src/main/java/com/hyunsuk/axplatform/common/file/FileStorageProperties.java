package com.hyunsuk.axplatform.common.file;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    private String uploadPath;

    @PostConstruct
    public void init() {
        System.out.println("[파일 저장 경로] uploadPath = " + uploadPath);
    }
}
