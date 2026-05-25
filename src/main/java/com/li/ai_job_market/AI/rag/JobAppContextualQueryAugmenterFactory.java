package com.li.ai_job_market.AI.rag;


import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建自定义的 RAG 查询增强器工厂类，后续可在此类中添加创建不同类型 RAG 查询增强器的方法
 */
public class JobAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextpromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以查看https://github.com/53214mm/li-ai-agent
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextpromptTemplate)
                .build();
    }

}
