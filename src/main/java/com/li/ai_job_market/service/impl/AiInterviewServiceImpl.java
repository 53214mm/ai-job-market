package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.li.ai_job_market.AI.app.JobApp;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.AiChatSessionMapper;
import com.li.ai_job_market.mapper.AiInterviewRecordMapper;
import com.li.ai_job_market.mapper.JobMapper;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.service.AiInterviewService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/** AI模拟面试服务实现，支持AI自动出题、答题评估和面试报告生成 */
@Slf4j
@Service
public class AiInterviewServiceImpl implements AiInterviewService {

    @Resource private JobApp jobApp;
    @Resource private JobMapper jobMapper;
    @Resource private AiInterviewRecordMapper recordMapper;
    @Resource private AiChatSessionMapper sessionMapper;

    @Override
    public AiInterviewRecord startInterview(Long userId, Long jobId) {
        Job job = jobMapper.selectById(jobId);
        ThrowUtils.throwIf(job == null, ErrorCode.NOT_FOUND_ERROR, "职位不存在");

        // 创建面试会话（复用chat_session表）
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setSessionType("INTERVIEW_PRACTICE");
        session.setTitle("AI模拟面试 - " + job.getTitle());
        session.setMessageCount(0);
        sessionMapper.insert(session);

        // AI 生成第一道题
        String prompt = """
            你是资深技术面试官。请根据职位描述生成一道专业面试题。
            职位：%s
            要求：%s
            以 JSON 返回：{"question":"...","keywords":"逗号分隔关键词","type":"TECHNICAL"}
            只返回 JSON。
            """.formatted(job.getTitle(), StringUtils.defaultString(job.getRequirement(), job.getDescription()));

        String result = jobApp.doChat(prompt, "interview-gen-" + jobId + "-" + System.currentTimeMillis());
        String question = extractJsonField(result, "question");
        String keywords = extractJsonField(result, "keywords");

        AiInterviewRecord record = new AiInterviewRecord();
        record.setUserId(userId);
        record.setJobId(jobId);
        record.setSessionId(session.getId());
        record.setQuestion(StringUtils.defaultString(question, "请简单介绍一下你自己和你的技术背景"));
        record.setExpectedAnswerKeywords(StringUtils.defaultString(keywords, "技术背景,项目经验,核心技能"));
        record.setQuestionIndex(1);
        recordMapper.insert(record);

        return record;
    }

    @Override
    public Map<String, Object> evaluateAnswer(Long recordId, Long userId, String answer) {
        AiInterviewRecord record = recordMapper.selectById(recordId);
        ThrowUtils.throwIf(record == null, ErrorCode.NOT_FOUND_ERROR, "面试记录不存在");

        // AI 评分
        String prompt = """
            你是资深面试官。评估以下面试回答。
            题目：%s
            期望关键词：%s
            回答：%s

            给出 0-100 评分和具体建议。
            以 JSON 返回：{"score":85,"feedback":"..."}
            只返回 JSON。
            """.formatted(record.getQuestion(), record.getExpectedAnswerKeywords(), answer);

        String result = jobApp.doChat(prompt, "interview-eval-" + recordId);
        int score = parseIntSafe(extractJsonField(result, "score"), 70);
        String feedback = extractJsonField(result, "feedback");

        record.setUserAnswer(answer);
        record.setScore(score);
        record.setAiFeedback(StringUtils.defaultString(feedback, "请继续保持"));
        recordMapper.updateById(record);

        // 生成下一题
        AiInterviewRecord nextQuestion = generateNextQuestion(userId, record);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("score", score);
        resp.put("feedback", feedback);
        resp.put("nextQuestion", nextQuestion);
        return resp;
    }

    @Override
    public Map<String, Object> getReport(Long sessionId) {
        List<AiInterviewRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<AiInterviewRecord>().eq(AiInterviewRecord::getSessionId, sessionId));

        int totalScore = records.stream().filter(r -> r.getScore() != null).mapToInt(AiInterviewRecord::getScore).sum();
        long count = records.stream().filter(r -> r.getScore() != null).count();
        long answered = records.stream().filter(r -> StringUtils.isNotBlank(r.getUserAnswer())).count();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalQuestions", records.size());
        report.put("answeredQuestions", answered);
        report.put("averageScore", count > 0 ? totalScore / (int) count : 0);
        report.put("suggestion", count > 0 && totalScore / (int) count >= 80
            ? "表现优秀，建议继续投递心仪职位"
            : "建议针对性提升薄弱环节后再次尝试");
        report.put("records", records);
        return report;
    }

    private AiInterviewRecord generateNextQuestion(Long userId, AiInterviewRecord prev) {
        if (prev.getQuestionIndex() >= 5) return null; // 最多5题

        Job job = jobMapper.selectById(prev.getJobId());
        String prompt = """
            继续面试。上一题是关于"%s"，现在出第%d题。
            职位：%s
            以 JSON 返回：{"question":"...","keywords":"..."}
            只返回 JSON。
            """.formatted(prev.getQuestion(), prev.getQuestionIndex() + 1, job != null ? job.getTitle() : "");

        String result = jobApp.doChat(prompt, "interview-next-" + prev.getSessionId());
        String question = extractJsonField(result, "question");
        String keywords = extractJsonField(result, "keywords");

        AiInterviewRecord record = new AiInterviewRecord();
        record.setUserId(userId);
        record.setJobId(prev.getJobId());
        record.setSessionId(prev.getSessionId());
        record.setQuestion(StringUtils.defaultString(question, "请继续介绍你的项目经验"));
        record.setExpectedAnswerKeywords(StringUtils.defaultString(keywords, "项目经验,技术细节"));
        record.setQuestionIndex(prev.getQuestionIndex() + 1);
        recordMapper.insert(record);
        return record;
    }

    private String extractJsonField(String json, String field) {
        if (StringUtils.isBlank(json)) return null;
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start < 0) {
            key = "\"" + field + "\":";
            start = json.indexOf(key);
            if (start < 0) return null;
            int valStart = start + key.length();
            int valEnd = json.indexOf(",", valStart);
            if (valEnd < 0) valEnd = json.indexOf("}", valStart);
            return valEnd > valStart ? json.substring(valStart, valEnd).trim() : null;
        }
        int valStart = start + key.length();
        int valEnd = json.indexOf("\"", valStart);
        return valEnd > valStart ? json.substring(valStart, valEnd) : null;
    }

    private int parseIntSafe(String s, int def) {
        if (StringUtils.isBlank(s)) return def;
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); }
        catch (NumberFormatException e) { return def; }
    }
}
