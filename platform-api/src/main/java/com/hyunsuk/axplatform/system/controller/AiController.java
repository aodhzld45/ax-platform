package com.hyunsuk.axplatform.system.controller;

import com.hyunsuk.axplatform.system.dto.AiTestRequest;
import com.hyunsuk.axplatform.system.dto.AiTestResponse;
import com.hyunsuk.axplatform.system.service.AiService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    @PostMapping("/test")
    public ResponseEntity<AiTestResponse> test(
            @RequestBody AiTestRequest request
    ) {
        AiTestResponse response = aiService.test(request);

        return ResponseEntity.ok(response);
    }

}
