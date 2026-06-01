package com.li.ai_job_market.config;

import com.li.ai_job_market.AI.rag.JobAppDocumentLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * PostgreSQL pgvector 向量存储配置
 * <p>
 * 使用独立的 PostgreSQL 数据源（与 MySQL 主数据源分离），
 * 将 Embedding 模型生成的向量存入 pgvector 扩展表。
 * <p>
 * 首次启动时从 classpath:document/*.md 加载文档并 Embedding，
 * 之后重启检测到表中已有数据则跳过，避免重复调用 Embedding API。
 */
@Slf4j
@Configuration
public class PgVectorConfig {

    @Resource
    private JobAppDocumentLoader jobAppDocumentLoader;

    @Bean
    @ConfigurationProperties(prefix = "pgvector.datasource")
    public DataSourceProperties pgvectorDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource pgvectorDataSource(
            @Qualifier("pgvectorDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate pgvectorJdbcTemplate(
            @Qualifier("pgvectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * pgvector 向量存储 Bean，首次启动自动加载文档，后续启动跳过
     */
    @Bean
    public VectorStore jobAppVectorStore(
            @Qualifier("pgvectorJdbcTemplate") JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            @Value("${pgvector.store.dimensions:1024}") int dimensions,
            @Value("${pgvector.store.distance-type:COSINE_DISTANCE}") String distanceType,
            @Value("${pgvector.store.max-document-batch-size:10000}") int maxBatchSize) {

        PgVectorStore.PgDistanceType distType;
        try {
            distType = PgVectorStore.PgDistanceType.valueOf(distanceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            distType = PgVectorStore.PgDistanceType.COSINE_DISTANCE;
        }

        // 确保 pgvector 扩展和 vector_store 表存在
        ensureSchemaExists(jdbcTemplate, dimensions, distanceType);

        PgVectorStore store = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .distanceType(distType)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .maxDocumentBatchSize(maxBatchSize)
                .build();

        // 按文件名去重加载，只插入新增的文档
        loadNewDocuments(store, jdbcTemplate);

        return store;
    }

    /**
     * 创建 pgvector 扩展和 vector_store 表（如果不存在）
     */
    private void ensureSchemaExists(JdbcTemplate jdbcTemplate, int dimensions, String distanceType) {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
            String createSql = String.format(
                    "CREATE TABLE IF NOT EXISTS public.vector_store (" +
                    "id uuid NOT NULL DEFAULT uuid_generate_v4(), " +
                    "content text NULL, " +
                    "metadata jsonb NULL, " +
                    "embedding vector(%d) NULL, " +
                    "CONSTRAINT vector_store_pkey PRIMARY KEY (id))", dimensions);
            jdbcTemplate.execute(createSql);
            log.info("pgvector 扩展和 vector_store 表已就绪");
        } catch (Exception e) {
            log.warn("创建 pgvector 表失败: {}", e.getMessage());
        }
    }

    /**
     * 加载文档到 pgvector，已存在的（按文件名判断）跳过，只插入新增文档
     */
    private void loadNewDocuments(PgVectorStore store, JdbcTemplate jdbcTemplate) {
        List<Document> allDocuments = jobAppDocumentLoader.loadMarkdowns();
        if (allDocuments.isEmpty()) {
            log.warn("未找到任何文档，跳过向量化");
            return;
        }

        // 查询已存在的文件名集合
        java.util.Set<String> existingNames = new java.util.HashSet<>();
        try {
            List<String> names = jdbcTemplate.queryForList(
                    "SELECT metadata->>'filename' FROM vector_store WHERE metadata->>'filename' IS NOT NULL",
                    String.class);
            existingNames.addAll(names);
            log.info("pgvector 已有 {} 个不同文件，本次扫描到 {} 个文件",
                    existingNames.size(), allDocuments.size());
        } catch (Exception e) {
            log.info("vector_store 表尚未创建，开始首次加载");
        }

        // 过滤出新增文档
        List<Document> newDocuments = allDocuments.stream()
                .filter(doc -> {
                    String filename = (String) doc.getMetadata().get("filename");
                    return filename == null || !existingNames.contains(filename);
                })
                .toList();

        if (newDocuments.isEmpty()) {
            log.info("无新增文档，跳过向量化");
            return;
        }

        log.info("新增 {} 篇文档，开始向量化并写入 pgvector（每批最多25条）...", newDocuments.size());
        int batchSize = 10;
        for (int i = 0; i < newDocuments.size(); i += batchSize) {
            int end = Math.min(i + batchSize, newDocuments.size());
            List<Document> batch = newDocuments.subList(i, end);
            store.add(batch);
            log.info("已写入第 {}-{} 条 / 共 {} 条", i + 1, end, newDocuments.size());
        }
        log.info("新增文档向量化完成");
    }
}
