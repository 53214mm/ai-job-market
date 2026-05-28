package com.li.ai_job_market.controller;

import com.li.ai_job_market.model.vo.MessageVO;
import com.li.ai_job_market.service.MessageService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class StompMessageController {

    @Resource private MessageService messageService;

    @MessageMapping("/messages")
    public void handleMessage(@Payload StompMessagePayload payload, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        messageService.send(senderId, payload.getReceiverId(),
                payload.getApplicationId(), payload.getContent());
        // WebSocket 推送在 MessageServiceImpl.send() 中完成
    }

    @Data
    public static class StompMessagePayload {
        private Long receiverId;
        private Long applicationId;
        private String content;
    }
}
