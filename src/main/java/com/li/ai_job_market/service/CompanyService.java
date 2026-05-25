package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.dto.company.*;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.*;

import java.util.List;

public interface CompanyService extends IService<Company> {

    long createCompany(Long userId, CompanyCreateRequest req);
    CompanyVO getCompanyDetail(Long companyId);
    boolean updateCompany(Long companyId, Long userId, CompanyUpdateRequest req);
    boolean verifyCompany(Long companyId, boolean verified);
    Page<CompanyVO> listCompanies(CompanyQueryRequest req);

    CompanyReview addReview(Long companyId, Long userId, CompanyReviewRequest req);
    List<CompanyReviewVO> listReviews(Long companyId);
    boolean updateReviewStatus(Long reviewId, String status);

    String aiGenerateDescription(Long companyId, Long userId);

    CompanyVO toCompanyVO(Company company);
}
