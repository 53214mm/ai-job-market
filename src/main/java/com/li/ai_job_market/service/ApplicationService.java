package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.dto.application.InterviewRequest;
import com.li.ai_job_market.model.entity.Application;
import com.li.ai_job_market.model.entity.Interview;
import com.li.ai_job_market.model.vo.ApplicationVO;
import com.li.ai_job_market.model.vo.FavoriteVO;

import java.util.List;

public interface ApplicationService extends IService<Application> {

    long apply(Long seekerId, Long jobId, Long resumeId, String coverLetter);
    ApplicationVO getDetail(Long id, Long userId);
    boolean updateStatus(Long id, Long operatorId, String status, String remark);

    Page<ApplicationVO> listMyApplications(Long seekerId, int current, int size, String status);
    Page<ApplicationVO> listReceivedApplications(Long recruiterId, int current, int size, String status);

    Interview scheduleInterview(Long applicationId, Long recruiterId, InterviewRequest req);
    boolean updateInterviewFeedback(Long iid, Long recruiterId, String feedback);

    long addFavorite(Long userId, String targetType, Long targetId);
    boolean removeFavorite(Long id, Long userId);
    List<FavoriteVO> listFavorites(Long userId);
}
