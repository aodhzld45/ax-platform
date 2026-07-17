package com.hyunsuk.axplatform.common.file.type;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileAssetType {
// 자산 단위로 세분화

    KOREAN_SOURCE_DOCUMENT(
            "document/korean-source",
            "국문 원천 문서",
            Set.of("pdf", "hwp", "hwpx", "docx", "txt", "csv", "json")
    ),

    PARALLEL_CORPUS(
            "dataset/parallel-corpus",
            "한국어-수어 병렬 말뭉치",
            Set.of("json", "jsonl", "csv", "parquet")
    ),

    GLOSS_DICTIONARY(
            "dataset/gloss-dictionary",
            "글로스 사전",
            Set.of("json", "jsonl", "csv")
    ),

    GRAMMAR_RULE(
            "dataset/grammar-rule",
            "수어 문법 규칙",
            Set.of("json", "yaml", "yml", "csv")
    ),

    SIGN_MOTION(
            "motion/sign",
            "수어 모션",
            Set.of("bvh", "fbx", "glb", "gltf", "json")
    ),

    NON_MANUAL_MOTION(
            "motion/non-manual",
            "비수지 표현",
            Set.of("json", "bvh", "fbx", "glb", "gltf")
    ),

    AVATAR_MODEL(
            "avatar/model",
            "3D 아바타 모델",
            Set.of("fbx", "glb", "gltf")
    ),

    AVATAR_TEXTURE(
            "avatar/texture",
            "아바타 텍스처",
            Set.of("png", "jpg", "jpeg", "webp")
    ),

    AVATAR_CONFIG(
            "avatar/config",
            "아바타 설정",
            Set.of("json", "yaml", "yml")
    ),

    AUDIO_INPUT(
            "media/audio-input",
            "음성 입력",
            Set.of("wav", "mp3", "m4a", "ogg")
    ),

    JOB_INPUT(
            "job",
            "AI 작업 입력",
            Set.of("txt", "json", "pdf", "wav", "mp3", "m4a")
    ),

    JOB_INTERMEDIATE(
            "job",
            "AI 작업 중간 결과",
            Set.of("json", "jsonl", "glb", "gltf")
    ),

    JOB_OUTPUT(
            "job",
            "AI 작업 결과",
            Set.of("mp4", "webm", "glb", "gltf", "png", "jpg", "json")
    );

    private final String baseDirectory;
    private final String description;
    private final Set<String> allowedExtensions;

    public boolean supports(String extension) {
        if (extension == null) {
            return false;
        }

        return allowedExtensions.contains(extension.toLowerCase());
    }
}