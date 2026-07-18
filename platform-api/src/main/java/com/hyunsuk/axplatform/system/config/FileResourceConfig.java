package com.hyunsuk.axplatform.system.config;

import java.nio.file.Path;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import com.hyunsuk.axplatform.common.file.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
@RequiredArgsConstructor
public class FileResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rootPath = Path.of(fileStorageProperties.getUploadPath())
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/files/**")
                .addResourceLocations(toDirectoryLocation(rootPath));
    }

    private String toDirectoryLocation(Path rootPath) {
        String location = rootPath.toUri().toString();

        if (location.endsWith("/")) {
            return location;
        }

        return location + "/";
    }
}
