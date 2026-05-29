package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.AI.app.JobApp;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.*;
import com.li.ai_job_market.model.dto.company.*;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.*;
import com.li.ai_job_market.service.CompanyService;
import com.li.ai_job_market.service.UserRecruiterProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 公司管理服务实现，提供公司信息管理、评价审核和AI生成公司介绍功能 */
@Slf4j
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company>
        implements CompanyService {

    @Resource
    private JobApp jobApp;

    @Resource
    private CompanyReviewMapper reviewMapper;

    @Resource
    private UserRecruiterProfileService recruiterProfileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createCompany(Long userId, CompanyCreateRequest req) {
        ThrowUtils.throwIf(StringUtils.isBlank(req.getName()),
                ErrorCode.PARAMS_ERROR, "公司名称不能为空");

        UserRecruiterProfile profile = recruiterProfileService.getOne(
                new LambdaQueryWrapper<UserRecruiterProfile>().eq(UserRecruiterProfile::getUserId, userId));
        if (profile != null && profile.getCompanyId() != null) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "您已关联公司，不可重复创建");
        }

        Company company = new Company();
        company.setName(req.getName());
        company.setShortName(req.getShortName());
        company.setIndustry(req.getIndustry());
        company.setScale(req.getScale());
        company.setStage(req.getStage());
        company.setWebsite(req.getWebsite());
        company.setAddress(req.getAddress());
        company.setDescription(req.getDescription());
        company.setCulture(req.getCulture());
        company.setWelfare(req.getWelfare());
        company.setVerified(false);
        company.setStatus("ACTIVE");

        boolean saved = this.save(company);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "创建公司失败");

        if (profile != null) {
            profile.setCompanyId(company.getId());
            recruiterProfileService.updateById(profile);
        }
        log.info("公司创建: id={}, name={}, userId={}", company.getId(), req.getName(), userId);
        return company.getId();
    }

    @Override
    public CompanyVO getCompanyDetail(Long companyId) {
        Company company = this.getById(companyId);
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "公司不存在");

        CompanyVO vo = toCompanyVO(company);
        vo.setReviews(listReviews(companyId));
        if (!vo.getReviews().isEmpty()) {
            vo.setAvgRating(vo.getReviews().stream()
                    .mapToInt(CompanyReviewVO::getRating).average().orElse(0));
        }
        vo.setJobCount(0);
        return vo;
    }

    @Override
    public boolean updateCompany(Long companyId, Long userId, CompanyUpdateRequest req) {
        UserRecruiterProfile profile = recruiterProfileService.getOne(
                new LambdaQueryWrapper<UserRecruiterProfile>().eq(UserRecruiterProfile::getUserId, userId));
        ThrowUtils.throwIf(profile == null || !companyId.equals(profile.getCompanyId()),
                ErrorCode.NO_AUTH_ERROR, "无权操作此公司");

        Company company = new Company();
        company.setId(companyId);
        if (req.getName() != null) company.setName(req.getName());
        if (req.getShortName() != null) company.setShortName(req.getShortName());
        if (req.getIndustry() != null) company.setIndustry(req.getIndustry());
        if (req.getScale() != null) company.setScale(req.getScale());
        if (req.getStage() != null) company.setStage(req.getStage());
        if (req.getWebsite() != null) company.setWebsite(req.getWebsite());
        if (req.getAddress() != null) company.setAddress(req.getAddress());
        if (req.getDescription() != null) company.setDescription(req.getDescription());
        if (req.getCulture() != null) company.setCulture(req.getCulture());
        if (req.getWelfare() != null) company.setWelfare(req.getWelfare());
        return this.updateById(company);
    }

    @Override
    public boolean verifyCompany(Long companyId, boolean verified) {
        Company company = new Company();
        company.setId(companyId);
        company.setVerified(verified);
        return this.updateById(company);
    }

    @Override
    public Page<CompanyVO> listCompanies(CompanyQueryRequest req) {
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Company::getStatus, "ACTIVE");
        wrapper.like(StringUtils.isNotBlank(req.getKeyword()), Company::getName, req.getKeyword());
        wrapper.eq(StringUtils.isNotBlank(req.getIndustry()), Company::getIndustry, req.getIndustry());
        wrapper.eq(StringUtils.isNotBlank(req.getScale()), Company::getScale, req.getScale());
        wrapper.orderByDesc(Company::getCreatedAt);

        Page<Company> page = this.page(new Page<>(req.getCurrent(), req.getPageSize()), wrapper);
        Page<CompanyVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toCompanyVO).toList());
        return voPage;
    }

    // ==================== 评价 ====================

    @Override
    public CompanyReview addReview(Long companyId, Long userId, CompanyReviewRequest req) {
        ThrowUtils.throwIf(req.getRating() == null || req.getRating() < 1 || req.getRating() > 5,
                ErrorCode.PARAMS_ERROR, "评分范围为 1-5");

        CompanyReview review = new CompanyReview();
        review.setCompanyId(companyId);
        review.setUserId(userId);
        review.setRating(req.getRating());
        review.setTitle(req.getTitle());
        review.setContent(req.getContent());
        review.setPros(req.getPros());
        review.setCons(req.getCons());
        review.setStatus("PENDING");
        reviewMapper.insert(review);
        return review;
    }

    @Override
    public List<CompanyReviewVO> listReviews(Long companyId) {
        LambdaQueryWrapper<CompanyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyReview::getCompanyId, companyId)
               .eq(CompanyReview::getStatus, "APPROVED")
               .orderByDesc(CompanyReview::getCreatedAt);
        return reviewMapper.selectList(wrapper).stream().map(this::toReviewVO).toList();
    }

    @Override
    public boolean updateReviewStatus(Long reviewId, String status) {
        CompanyReview review = new CompanyReview();
        review.setId(reviewId);
        review.setStatus(status);
        return reviewMapper.updateById(review) > 0;
    }

    // ==================== AI ====================

    @Override
    public String aiGenerateDescription(Long companyId, Long userId) {
        Company company = this.getById(companyId);
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "公司不存在");

        UserRecruiterProfile profile = recruiterProfileService.getOne(
                new LambdaQueryWrapper<UserRecruiterProfile>().eq(UserRecruiterProfile::getUserId, userId));
        ThrowUtils.throwIf(profile == null || !companyId.equals(profile.getCompanyId()),
                ErrorCode.NO_AUTH_ERROR, "无权操作");

        String prompt = """
            你是一位专业的公司介绍写手。请根据以下公司信息，生成一段专业的公司介绍文案（300-500字）。

            公司名称：%s
            行业：%s
            规模：%s
            融资阶段：%s
            企业文化：%s
            福利待遇：%s

            要求：语言正式、突出亮点、适合展示在招聘网站的公司主页。只返回纯文本。
            """.formatted(
                company.getName(),
                StringUtils.defaultString(company.getIndustry(), "未填写"),
                StringUtils.defaultString(company.getScale(), "未填写"),
                StringUtils.defaultString(company.getStage(), "未填写"),
                StringUtils.defaultString(company.getCulture(), "未填写"),
                StringUtils.defaultString(company.getWelfare(), "未填写")
            );

        return jobApp.doChat(prompt, "company-describe-" + companyId);
    }

    // ==================== 辅助方法 ====================

    @Override
    public CompanyVO toCompanyVO(Company c) {
        CompanyVO vo = new CompanyVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setShortName(c.getShortName());
        vo.setLogoUrl(c.getLogoUrl());
        vo.setIndustry(c.getIndustry());
        vo.setScale(c.getScale());
        vo.setStage(c.getStage());
        vo.setWebsite(c.getWebsite());
        vo.setAddress(c.getAddress());
        vo.setDescription(c.getDescription());
        vo.setCulture(c.getCulture());
        vo.setWelfare(c.getWelfare());
        vo.setVerified(c.getVerified());
        vo.setStatus(c.getStatus());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }

    private CompanyReviewVO toReviewVO(CompanyReview r) {
        CompanyReviewVO vo = new CompanyReviewVO();
        vo.setId(r.getId());
        vo.setCompanyId(r.getCompanyId());
        vo.setUserId(r.getUserId());
        vo.setRating(r.getRating());
        vo.setTitle(r.getTitle());
        vo.setContent(r.getContent());
        vo.setPros(r.getPros());
        vo.setCons(r.getCons());
        vo.setStatus(r.getStatus());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
