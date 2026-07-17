package com.hyunsuk.axplatform.common.file.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobFileStage {

    // 작업 단계 enum - AI 작업 결과는 JOB_INPUT, JOB_INTERMEDIATE, JOB_OUTPUT만으로 끝내지 않고 단계를 분리

    INPUT("input"),
    INTERMEDIATE("intermediate"),
    OUTPUT("output"),
    LOG("log");

    private final String directory;
}
