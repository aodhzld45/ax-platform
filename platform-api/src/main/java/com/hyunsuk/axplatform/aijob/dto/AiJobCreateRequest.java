package com.hyunsuk.axplatform.aijob.dto;

import com.hyunsuk.axplatform.aijob.entity.AiJobType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiJobCreateRequest {

    private AiJobType jobType = AiJobType.KOREAN_TO_GLOSS;
}
