package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.annotation.AuthCheck;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.model.entity.Article;
import com.li.ai_job_market.model.entity.ArticleCategory;
import com.li.ai_job_market.model.vo.UserVO;
import com.li.ai_job_market.service.ArticleService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Resource private ArticleService articleService;
    @Resource private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    // === 前台接口 ===

    @GetMapping
    public BaseResponse<Page<Article>> list(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) String keyword) {
        return ResultUtils.success(articleService.listPublished(current, size, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public BaseResponse<Article> detail(@PathVariable Long id) {
        return ResultUtils.success(articleService.getDetail(id));
    }

    @GetMapping("/categories")
    public BaseResponse<List<ArticleCategory>> categories() {
        return ResultUtils.success(articleService.listCategories());
    }

    // === 管理后台接口 ===

    @GetMapping("/admin/all")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Page<Article>> listAll(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long categoryId) {
        return ResultUtils.success(articleService.listAll(current, size, status, categoryId));
    }

    @PostMapping
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Long> create(@RequestBody ArticleForm form, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(articleService.createArticle(user.getId(),
                form.getTitle(), form.getContent(), form.getCategoryId(),
                form.getSummary(), form.getCoverUrl(), form.getTags()));
    }

    @PutMapping("/{id}")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Boolean> update(@PathVariable Long id, @RequestBody ArticleForm form,
                                        HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(articleService.updateArticle(id, user.getId(),
                form.getTitle(), form.getContent(), form.getCategoryId(),
                form.getSummary(), form.getCoverUrl(), form.getTags()));
    }

    @PutMapping("/{id}/publish")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Boolean> publish(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(articleService.publishArticle(id, user.getId()));
    }

    @PutMapping("/{id}/unpublish")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Boolean> unpublish(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(articleService.unpublishArticle(id, user.getId()));
    }
}

@Data
class ArticleForm {
    private String title;
    private String content;
    private Long categoryId;
    private String summary;
    private String coverUrl;
    private String tags;
}
