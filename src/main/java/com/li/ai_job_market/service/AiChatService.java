package com.li.ai_job_market.service;

import com.li.ai_job_market.model.entity.AiChatMessage;
import com.li.ai_job_market.model.entity.AiChatSession;

import java.util.List;

public interface AiChatService {
    Long createSession(Long userId, String type, String title);
    List<AiChatSession> listSessions(Long userId);
    String sendMessage(Long sessionId, Long userId, String message);
    reactor.core.publisher.Flux<String> sendMessageStream(Long sessionId, Long userId, String message);
    List<AiChatMessage> getMessages(Long sessionId);
}
