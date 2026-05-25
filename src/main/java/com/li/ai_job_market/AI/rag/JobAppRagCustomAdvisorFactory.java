package com.li.ai_job_market.AI.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 增强顾问工厂类，后续可在此类中添加创建不同类型 RAG 增强顾问的方法
 */
public class JobAppRagCustomAdvisorFactory {

    /**
     * 创建一个基于向量数据库的 RAG 增强顾问，示例中通过过滤表达式筛选出特定状态的文档进行增强
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore,String status) {
        // 构建过滤表达式，筛选出状态为指定值的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) // 通过过滤表达式筛选出特定状态的文档进行增强
                .similarityThreshold(0.5) // 设置相似度阈值，只有相似度高于该值的文档才会被检索到
                .topK(3) // 设置返回的文档数量，示例中返回最相似的3条文档
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(JobAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }

}
