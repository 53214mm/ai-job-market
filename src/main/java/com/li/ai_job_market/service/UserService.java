package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.User;
import com.li.ai_job_market.model.vo.LoginResult;
import com.li.ai_job_market.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/** 用户服务接口，提供用户注册、登录、认证和用户信息管理功能 */
public interface UserService extends IService<User> {

    long register(String email, String password, String checkPassword, String role, String nickname);

    LoginResult login(String email, String password);

    UserVO getLoginUser(HttpServletRequest request);

    UserVO getLoginUser(String token);

    boolean updateUser(User user);

    UserVO toUserVO(User user);
}
