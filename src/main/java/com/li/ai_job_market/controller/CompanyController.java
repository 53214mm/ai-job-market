package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.annotation.AuthCheck;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.model.dto.company.*;
import com.li.ai_job_market.model.entity.CompanyReview;
import com.li.ai_job_market.model.enums.UserRoleEnum;
import com.li.ai_job_market.model.vo.*;
import com.li.ai_job_market.service.CompanyService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/companies")
public class CompanyController {

    @Resource
    private CompanyService companyService;

    @Resource
    private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    // ==================== 公司 CRUD ====================

    @GetMapping
    public BaseResponse<Page<CompanyVO>> list(CompanyQueryRequest req) {
        return ResultUtils.success(companyService.listCompanies(req));
    }

    @GetMapping("/{id}")
    public BaseResponse<CompanyVO> detail(@PathVariable Long id) {
        return ResultUtils.success(companyService.getCompanyDetail(id));
    }

    @PostMapping
    public BaseResponse<Long> create(@RequestBody CompanyCreateRequest req,
                                      HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        ThrowUtils.throwIf(!UserRoleEnum.RECRUITER.getValue().equals(loginUser.getRole()),
                ErrorCode.NO_AUTH_ERROR, "仅招聘方可创建公司");
        return ResultUtils.success(companyService.createCompany(loginUser.getId(), req));
    }

    @PutMapping("/{id}")
    public BaseResponse<Boolean> update(@PathVariable Long id,
                                         @RequestBody CompanyUpdateRequest req,
                                         HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(companyService.updateCompany(id, loginUser.getId(), req));
    }

    @PutMapping("/{id}/verify")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Boolean> verify(@PathVariable Long id,
                                         @RequestParam boolean verified) {
        return ResultUtils.success(companyService.verifyCompany(id, verified));
    }

    // ==================== 评价 ====================

    @PostMapping("/{id}/reviews")
    public BaseResponse<CompanyReview> addReview(@PathVariable Long id,
                                                  @RequestBody CompanyReviewRequest req,
                                                  HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(companyService.addReview(id, loginUser.getId(), req));
    }

    @GetMapping("/{id}/reviews")
    public BaseResponse<List<CompanyReviewVO>> listReviews(@PathVariable Long id) {
        return ResultUtils.success(companyService.listReviews(id));
    }

    @PutMapping("/reviews/{rid}/status")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Boolean> updateReviewStatus(@PathVariable Long rid,
                                                     @RequestParam String status) {
        return ResultUtils.success(companyService.updateReviewStatus(rid, status));
    }

    // ==================== AI ====================

    @PostMapping("/{id}/ai-describe")
    public BaseResponse<String> aiDescribe(@PathVariable Long id,
                                            HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(companyService.aiGenerateDescription(id, loginUser.getId()));
    }
}
