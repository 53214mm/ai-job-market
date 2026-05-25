package com.li.ai_job_market.AI.agent;


import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import com.li.ai_job_market.AI.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  抽象基础代理类，用于管理代理状态和执行流程。
 *  提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 *  子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 代理的名称
    private String name;
    // 代理的系统提示词
    private String systemPrompt;
    // 代理的下一步提示词
    private String nextStepPrompt;
    // 代理的状态
    private AgentState state = AgentState.IDLE;
    //当前执行步骤数
    private int currentStep = 0;
    // 代理的最大执行步骤数
    private int maxSteps = 10;
    // 代理的ChatClient实例
    private ChatClient chatClient;
    // 自主维护会话上下文
    private List<Message> messageList = new ArrayList<>();
    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 流式运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时

        CompletableFuture.runAsync(()->{
            try{
                if (this.state != AgentState.IDLE) {
                    emitter.send("Cannot run agent from state: " + this.state);
                    emitter.complete();
                    return;
                }
                if (StringUtil.isBlank(userPrompt)) {
                    emitter.send("Cannot run agent with empty user prompt");
                    emitter.complete();
                    return;
                }
            }catch (Exception e){
                emitter.completeWithError(e);
            }

            // 更改状态
            state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);
                    String stepResult = step();
                    boolean isFinal = state == AgentState.FINISHED;

                    // 检查是否包含 THINK/ACT 分隔符（工具调用步骤）
                    int actIdx = stepResult.indexOf("ACT: ");
                    if (actIdx > 0 && stepResult.startsWith("THINK: ")) {
                        String thinkText = stepResult.substring(7, actIdx).trim();
                        String actText = stepResult.substring(actIdx + 5).trim();
                        // 发送思考文字（折叠）
                        emitter.send("[思考]Step " + stepNumber + " 思考: " + thinkText);
                        // 发送行动结果（折叠）
                        String actPrefix = (isFinal && !thinkText.isEmpty()) ? "[回复]" : "[思考]";
                        emitter.send(actPrefix + "Step " + stepNumber + ": " + actText);
                    } else {
                        String prefix = isFinal ? "[回复]" : "[思考]";
                        emitter.send(prefix + "Step " + stepNumber + ": " + stepResult);
                    }
                }
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    emitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                }
                emitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    emitter.send("执行错误" + e.getMessage());
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                // 清理资源
                this.cleanup();
            }
            emitter.onTimeout(()->{
                this.state = AgentState.ERROR;
                this.cleanup();
                log.warn("Agent execution timed out");
            });
            emitter.onCompletion(()->{
                if(this.state==AgentState.RUNNING){
                    this.state = AgentState.FINISHED;
                }
                this.cleanup();
                log.info("Agent execution completed");
            });
        });
        return emitter;
    }
    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }


}
