package com.li.ai_job_market.AI.app;


import com.li.ai_job_market.AI.advisor.MyLoggerAdvisor;
import com.li.ai_job_market.AI.chatMemory.RedisBasedChatMemory;
import com.li.ai_job_market.AI.rag.JobAppRagCustomAdvisorFactory;
import com.li.ai_job_market.AI.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 核心应用类，封装对话、RAG 检索、工具调用等核心功能
 */
@Component
@Slf4j
public class JobApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = """
            你是 AI 智能招聘市场的专属助手，致力于为求职者和招聘方提供专业、高效的招聘服务。
            【你的职责】
            1. 解答求职相关疑问：简历优化、面试技巧、职业规划、行业趋势
            2. 辅助招聘方：职位描述优化、人才匹配建议、面试问题推荐
            3. 基于知识库提供准确的招聘市场信息
            【行为准则】
            - 回答简洁专业，避免冗长
            - 涉及薪资、公司评价等敏感信息时，基于知识库客观回答
            - 不确定的信息明确告知用户"建议进一步核实"
            - 拒绝回答与招聘求职无关的问题
            """;

    public JobApp(@Qualifier("dashScopeChatModel") ChatModel dashscopeChatModel, RedisBasedChatMemory redisBasedChatMemory) {
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(redisBasedChatMemory).build()
                        // 自定义日志 Advisor，可按需开启
                        ,new MyLoggerAdvisor()
                        // 注册敏感词拦截 Advisor
//                        ,new ProhibitedWordsAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                        ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    /**
     * AI 基础对话（支持多轮对话记忆）流式
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }

    record JobReport(String title, List<String> suggestions) {
    }

    /**
     * AI 对话报告 （结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public JobReport doChatWithReport(String message, String chatId) {
        JobReport jobReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后生成结构化报告，标题为{用户名}的求职分析报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(JobReport.class);
        return jobReport;
    }

    @Resource
    private VectorStore jobAppVectorStore;
    @Resource
    private QueryRewriter queryRewriter;

    /**
     * AI RAG对话（检索增强生成）
     * 从 pgvector 知识库检索相关文档后增强回答
     */
    public String doChatWithRag(String message, String chatId) {
        message = queryRewriter.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 基于 pgvector 的自定义 RAG 检索增强（文档查询器 + 上下文增强器）
                .advisors(
                        JobAppRagCustomAdvisorFactory.createJobRagAdvisor(jobAppVectorStore)
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    @Resource
    private ToolCallback[] allTools;
    /**
     * ai 对话工具调用示例
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;
    /**
     * ai 对话工具调用示例（使用 ToolCallbackProvider 动态提供工具）
     */
    public String doChatWithMcp(String message, String chatId) {
        System.out.println("MCP tools: " + toolCallbackProvider.getToolCallbacks());

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 使用 ToolCallbackProvider 动态提供工具
                .toolCallbacks(toolCallbackProvider)//mcp工具
                .toolCallbacks(allTools)//本地工具
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();

        return content;
    }


}
