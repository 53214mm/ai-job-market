package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.li.ai_job_market.AI.app.JobApp;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.AiChatMessageMapper;
import com.li.ai_job_market.mapper.AiChatSessionMapper;
import com.li.ai_job_market.model.entity.AiChatMessage;
import com.li.ai_job_market.model.entity.AiChatSession;
import com.li.ai_job_market.service.AiChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Resource private JobApp jobApp;
    @Resource private AiChatSessionMapper sessionMapper;
    @Resource private AiChatMessageMapper messageMapper;

    @Override
    public Long createSession(Long userId, String type, String title) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setSessionType(StringUtils.defaultString(type, "GENERAL"));
        session.setTitle(StringUtils.defaultString(title, "新对话"));
        session.setMessageCount(0);
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    public List<AiChatSession> listSessions(Long userId) {
        return sessionMapper.selectList(
            new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId)
                .orderByDesc(AiChatSession::getUpdatedAt));
    }

    @Override
    public String sendMessage(Long sessionId, Long userId, String message) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        ThrowUtils.throwIf(session == null, ErrorCode.NOT_FOUND_ERROR, "会话不存在");

        // 保存用户消息
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("USER");
        userMsg.setContent(message);
        messageMapper.insert(userMsg);

        // 调用 AI，chatId 格式关联会话，Redis 自动管理多轮上下文
        String chatId = "ai-chat-" + sessionId;
        String reply = jobApp.doChat(message, chatId);

        // 保存 AI 回复
        AiChatMessage aiMsg = new AiChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("ASSISTANT");
        aiMsg.setContent(reply);
        messageMapper.insert(aiMsg);

        // 更新计数
        session.setMessageCount(session.getMessageCount() + 1);
        sessionMapper.updateById(session);

        return reply;
    }

    @Override
    public List<AiChatMessage> getMessages(Long sessionId) {
        return messageMapper.selectList(
            new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getCreatedAt));
    }
}
