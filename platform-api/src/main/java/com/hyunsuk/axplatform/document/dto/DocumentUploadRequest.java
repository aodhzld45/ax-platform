package com.hyunsuk.axplatform.document.dto;

import com.hyunsuk.axplatform.common.file.type.FileAssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotNull
    private MultipartFile file;

    @NotBlank
    private String title;

    private FileAssetType assetType;
}
