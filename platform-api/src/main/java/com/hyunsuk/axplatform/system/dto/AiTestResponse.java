package com.hyunsuk.axplatform.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AiTestResponse {
        private String status;

        @JsonProperty("received_message")
        private String receivedMessage;

        private String result;

}