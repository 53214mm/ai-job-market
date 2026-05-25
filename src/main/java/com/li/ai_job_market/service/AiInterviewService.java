package com.li.ai_job_market.service;

import com.li.ai_job_market.model.entity.AiInterviewRecord;

import java.util.List;
import java.util.Map;

public interface AiInterviewService {
    AiInterviewRecord startInterview(Long userId, Long jobId);
    Map<String, Object> evaluateAnswer(Long recordId, Long userId, String answer);
    Map<String, Object> getReport(Long sessionId);
}
