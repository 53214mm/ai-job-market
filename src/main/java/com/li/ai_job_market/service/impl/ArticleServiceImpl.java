package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.ArticleCategoryMapper;
import com.li.ai_job_market.mapper.ArticleMapper;
import com.li.ai_job_market.model.entity.Article;
import com.li.ai_job_market.model.entity.ArticleCategory;
import com.li.ai_job_market.service.ArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {

    @Resource private ArticleCategoryMapper categoryMapper;

    @Override
    public long createArticle(Long userId, String title, String content, Long categoryId,
                              String summary, String coverUrl, String tags) {
        ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR, "标题不能为空");
        Article a = new Article();
        a.setAuthorId(userId);
        a.setTitle(title);
        a.setContent(content);
        a.setCategoryId(categoryId);
        a.setSummary(summary);
        a.setCoverUrl(coverUrl);
        a.setTags(tags);
        a.setViewCount(0);
        a.setStatus("DRAFT");
        this.save(a);
        return a.getId();
    }

    @Override
    public boolean updateArticle(Long id, Long userId, String title, String content,
                                 Long categoryId, String summary, String coverUrl, String tags) {
        Article a = this.getById(id);
        ThrowUtils.throwIf(a == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        if (title != null) a.setTitle(title);
        if (content != null) a.setContent(content);
        if (categoryId != null) a.setCategoryId(categoryId);
        if (summary != null) a.setSummary(summary);
        if (coverUrl != null) a.setCoverUrl(coverUrl);
        if (tags != null) a.setTags(tags);
        return this.updateById(a);
    }

    @Override
    public boolean publishArticle(Long id, Long userId) {
        Article a = this.getById(id);
        ThrowUtils.throwIf(a == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        a.setStatus("PUBLISHED");
        a.setPublishedAt(LocalDateTime.now());
        return this.updateById(a);
    }

    @Override
    public boolean unpublishArticle(Long id, Long userId) {
        Article a = this.getById(id);
        ThrowUtils.throwIf(a == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        a.setStatus("DRAFT");
        return this.updateById(a);
    }

    @Override
    public Page<Article> listPublished(int current, int size, Long categoryId, String keyword) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.eq(Article::getStatus, "PUBLISHED");
        w.eq(categoryId != null, Article::getCategoryId, categoryId);
        w.like(StringUtils.isNotBlank(keyword), Article::getTitle, keyword);
        w.orderByDesc(Article::getPublishedAt);
        return this.page(new Page<>(current, size), w);
    }

    @Override
    public Page<Article> listAll(int current, int size, String status, Long categoryId) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.isNotBlank(status), Article::getStatus, status);
        w.eq(categoryId != null, Article::getCategoryId, categoryId);
        w.orderByDesc(Article::getCreatedAt);
        return this.page(new Page<>(current, size), w);
    }

    @Override
    public Article getDetail(Long id) {
        Article a = this.getById(id);
        if (a != null) {
            a.setViewCount(a.getViewCount() + 1);
            this.updateById(new Article() {{ setId(id); setViewCount(a.getViewCount()); }});
        }
        return a;
    }

    @Override
    public List<ArticleCategory> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ArticleCategory>().orderByAsc(ArticleCategory::getSortOrder));
    }
}
