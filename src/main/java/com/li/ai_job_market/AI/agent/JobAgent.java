package com.li.ai_job_market.AI.agent;

import com.li.ai_job_market.AI.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 智能体核心实现，注册全部工具并通过思考-行动循环完成用户请求
 */
@Component
public class JobAgent extends ToolCallAgent {

    public JobAgent(ToolCallback[] allTools, @Qualifier("dashScopeChatModel")ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("liManus");
        String SYSTEM_PROMPT = """  
                你是jobAgent，一个全能的AI助手，旨在解决用户提出的任何任务。你拥有各种工具，可以调用这些工具来高效完成复杂的请求。 
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                根据用户需求，主动选择最合适的工具或工具组合。对于复杂任务，可以将问题分解，并逐步使用不同的工具来解决。使用每个工具后，清楚地解释执行结果并建议下一步。如果你想在任何时候停止互动，请使用 `terminate` 工具/函数调用。 
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}

