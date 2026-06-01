package com.li.ai_job_market.config;

import com.li.ai_job_market.AI.rag.JobAppDocumentLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL pgvector 向量存储配置
 * <p>
 * 管理三个向量存储：知识库（vector_store）、职位语义搜索（job_vectors）
 */
@Slf4j
@Configuration
public class PgVectorConfig {

    @Resource
    private JobAppDocumentLoader jobAppDocumentLoader;

    private DataSource createPgDataSource(String url, String user, String password) {
        return org.springframework.boot.jdbc.DataSourceBuilder.create()
                .url(url).username(user).password(password).build();
    }

    /** 共享的 PostgreSQL JdbcTemplate，供语义搜索复用 */
    @Bean
    public JdbcTemplate pgJdbcTemplate(
            @Value("${pgvector.datasource.url}") String url,
            @Value("${pgvector.datasource.username}") String user,
            @Value("${pgvector.datasource.password}") String password) {
        return new JdbcTemplate(createPgDataSource(url, user, password));
    }

    /** 向量存储 Bean（知识库 + 职位语义搜索共享 vector_store 表） */
    @Bean
    public VectorStore jobAppVectorStore(
            EmbeddingModel embeddingModel,
            @Value("${pgvector.datasource.url}") String pgUrl,
            @Value("${pgvector.datasource.username}") String pgUser,
            @Value("${pgvector.datasource.password}") String pgPassword,
            @Value("${pgvector.store.dimensions:1024}") int dimensions,
            @Value("${pgvector.store.distance-type:COSINE_DISTANCE}") String distanceType,
            @Value("${pgvector.store.max-document-batch-size:10000}") int maxBatchSize) {

        JdbcTemplate jt = new JdbcTemplate(createPgDataSource(pgUrl, pgUser, pgPassword));
        // 同时创建 job_vectors 表（用于 AI 语义搜索）
        jt.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jt.execute(String.format("CREATE TABLE IF NOT EXISTS public.job_vectors (" +
                "id uuid PRIMARY KEY, content text, metadata jsonb, embedding vector(%d))", dimensions));

        PgVectorStore.PgDistanceType distType;
        try {
            distType = PgVectorStore.PgDistanceType.valueOf(distanceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            distType = PgVectorStore.PgDistanceType.COSINE_DISTANCE;
        }

        ensureSchemaExists(jt, dimensions, distanceType);

        PgVectorStore store = PgVectorStore.builder(jt, embeddingModel)
                .dimensions(dimensions)
                .distanceType(distType)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .maxDocumentBatchSize(maxBatchSize)
                .build();

        loadNewDocuments(store, jt);
        return store;
    }

    private void ensureSchemaExists(JdbcTemplate jt, int dimensions, String distanceType) {
        try {
            jt.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
            jt.execute(String.format(
                    "CREATE TABLE IF NOT EXISTS public.vector_store (" +
                    "id uuid NOT NULL DEFAULT uuid_generate_v4(), " +
                    "content text, metadata jsonb, embedding vector(%d), " +
                    "CONSTRAINT vector_store_pkey PRIMARY KEY (id))", dimensions));
            log.info("vector_store 表已就绪");
        } catch (Exception e) {
            log.warn("创建 vector_store 失败: {}", e.getMessage());
        }
    }

    private void loadNewDocuments(PgVectorStore store, JdbcTemplate jt) {
        List<Document> all = jobAppDocumentLoader.loadMarkdowns();
        if (all.isEmpty()) { log.warn("未找到文档"); return; }

        java.util.Set<String> existing = new java.util.HashSet<>();
        try {
            existing.addAll(jt.queryForList(
                    "SELECT metadata->>'filename' FROM vector_store WHERE metadata->>'filename' IS NOT NULL", String.class));
        } catch (Exception e) { log.info("首次加载"); }

        List<Document> fresh = all.stream()
                .filter(d -> !existing.contains((String) d.getMetadata().get("filename")))
                .toList();
        if (fresh.isEmpty()) { log.info("无新增文档，跳过向量化"); return; }

        log.info("新增 {} 篇文档，写入 pgvector...", fresh.size());
        for (int i = 0; i < fresh.size(); i += 10) {
            store.add(fresh.subList(i, Math.min(i + 10, fresh.size())));
        }
        log.info("文档向量化完成");
    }
}
