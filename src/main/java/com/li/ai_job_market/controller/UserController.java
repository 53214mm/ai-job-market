package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.annotation.AuthCheck;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.model.dto.user.*;
import com.li.ai_job_market.model.entity.User;
import com.li.ai_job_market.model.vo.LoginResult;
import com.li.ai_job_market.model.vo.UserVO;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody RegisterRequest req) {
        long userId = userService.register(
                req.getEmail(), req.getPassword(), req.getCheckPassword(),
                req.getRole(), req.getNickname());
        return ResultUtils.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<LoginResult> login(@RequestBody LoginRequest req) {
        LoginResult result = userService.login(req.getEmail(), req.getPassword());
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        UserVO userVO = userService.getLoginUser(request);
        return ResultUtils.success(userVO);
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest req,
                                             HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!loginUser.getId().equals(req.getId()),
                ErrorCode.NO_AUTH_ERROR, "只能修改自己的信息");

        User user = new User();
        user.setId(req.getId());
        user.setNickname(req.getNickname());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setPhone(req.getPhone());
        boolean result = userService.updateUser(user);
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Page<UserVO>> listUsers(UserQueryRequest req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(req.getId() != null, User::getId, req.getId());
        wrapper.eq(StringUtils.isNotBlank(req.getEmail()), User::getEmail, req.getEmail());
        wrapper.eq(StringUtils.isNotBlank(req.getRole()), User::getRole, req.getRole());
        wrapper.eq(StringUtils.isNotBlank(req.getStatus()), User::getStatus, req.getStatus());
        wrapper.like(StringUtils.isNotBlank(req.getNickname()), User::getNickname, req.getNickname());
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = userService.page(
                new Page<>(req.getCurrent(), req.getPageSize()), wrapper);

        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(userService::toUserVO)
                .toList());

        return ResultUtils.success(voPage);
    }

    @PutMapping("/disable/{id}")
    @AuthCheck(mustRole = "ADMIN")
    public BaseResponse<Boolean> disableUser(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus("DISABLED");
        boolean result = userService.updateUser(user);
        return ResultUtils.success(result);
    }
}
