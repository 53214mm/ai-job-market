package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.Article;
import com.li.ai_job_market.model.entity.ArticleCategory;

import java.util.List;

/** 文章管理服务接口，提供文章的创建、编辑、发布和分类查询功能 */
public interface ArticleService extends IService<Article> {

    long createArticle(Long userId, String title, String content, Long categoryId,
                       String summary, String coverUrl, String tags);

    boolean updateArticle(Long id, Long userId, String title, String content,
                          Long categoryId, String summary, String coverUrl, String tags);

    boolean publishArticle(Long id, Long userId);

    boolean unpublishArticle(Long id, Long userId);

    Page<Article> listPublished(int current, int size, Long categoryId, String keyword);

    Page<Article> listAll(int current, int size, String status, Long categoryId);

    Article getDetail(Long id);

    // 文章分类
    List<ArticleCategory> listCategories();
}
