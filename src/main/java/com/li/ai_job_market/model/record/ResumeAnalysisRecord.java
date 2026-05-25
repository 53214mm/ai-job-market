package com.li.ai_job_market.model.record;

/**
 * AI 简历分析结构化输出 Record
 * 配合 JobApp.doChatWithReport() 使用
 */
public record ResumeAnalysisRecord(
    int overallScore,
    int formatScore,
    int contentScore,
    int keywordScore,
    String strengths,
    String weaknesses,
    String suggestions
) {}
