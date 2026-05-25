package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.BusinessException;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.UserMapper;
import com.li.ai_job_market.mapper.UserSeekerProfileMapper;
import com.li.ai_job_market.mapper.UserRecruiterProfileMapper;
import com.li.ai_job_market.model.entity.User;
import com.li.ai_job_market.model.entity.UserSeekerProfile;
import com.li.ai_job_market.model.entity.UserRecruiterProfile;
import com.li.ai_job_market.model.enums.UserRoleEnum;
import com.li.ai_job_market.model.vo.LoginResult;
import com.li.ai_job_market.model.vo.UserVO;
import com.li.ai_job_market.service.UserService;
import com.li.ai_job_market.utils.JwtUtils;
import com.li.ai_job_market.utils.PasswordUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private PasswordUtils passwordUtils;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private UserSeekerProfileMapper seekerProfileMapper;

    @Resource
    private UserRecruiterProfileMapper recruiterProfileMapper;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long register(String email, String password, String checkPassword,
                         String role, String nickname) {
        ThrowUtils.throwIf(StringUtils.isBlank(email), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(!EMAIL_PATTERN.matcher(email).matches(), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        ThrowUtils.throwIf(StringUtils.isBlank(password), ErrorCode.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(password.length() < 6, ErrorCode.PARAMS_ERROR, "密码至少6位");
        ThrowUtils.throwIf(!password.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        ThrowUtils.throwIf(UserRoleEnum.getEnumByValue(role) == null, ErrorCode.PARAMS_ERROR, "角色不合法");
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.getValue().equals(role),
                ErrorCode.PARAMS_ERROR, "不能注册管理员账号");
        ThrowUtils.throwIf(StringUtils.isBlank(nickname), ErrorCode.PARAMS_ERROR, "昵称不能为空");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        long count = this.count(wrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "该邮箱已注册");

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordUtils.encode(password));
        user.setRole(role);
        user.setNickname(nickname);
        user.setStatus("ACTIVE");
        boolean saved = this.save(user);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "注册失败");

        if (UserRoleEnum.SEEKER.getValue().equals(role)) {
            UserSeekerProfile profile = new UserSeekerProfile();
            profile.setUserId(user.getId());
            seekerProfileMapper.insert(profile);
        } else if (UserRoleEnum.RECRUITER.getValue().equals(role)) {
            UserRecruiterProfile profile = new UserRecruiterProfile();
            profile.setUserId(user.getId());
            recruiterProfileMapper.insert(profile);
        }

        log.info("新用户注册: id={}, email={}, role={}", user.getId(), email, role);
        return user.getId();
    }

    @Override
    public LoginResult login(String email, String password) {
        ThrowUtils.throwIf(StringUtils.isBlank(email), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(password), ErrorCode.PARAMS_ERROR, "密码不能为空");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email)
               .select(User::getId, User::getEmail, User::getPasswordHash,
                       User::getRole, User::getNickname, User::getAvatarUrl,
                       User::getStatus, User::getPhone, User::getLastLoginTime,
                       User::getCreatedAt);
        User user = this.getOne(wrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "邮箱或密码错误");

        ThrowUtils.throwIf("DISABLED".equals(user.getStatus()),
                ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");

        boolean matches = passwordUtils.matches(password, user.getPasswordHash());
        ThrowUtils.throwIf(!matches, ErrorCode.PARAMS_ERROR, "邮箱或密码错误");

        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        this.updateById(updateUser);

        log.info("用户登录成功: id={}, email={}", user.getId(), email);
        return new LoginResult(token, toUserVO(user));
    }

    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        ThrowUtils.throwIf(StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer "),
                ErrorCode.NOT_LOGIN_ERROR, "未登录");

        String token = authHeader.substring(7);
        return getLoginUser(token);
    }

    @Override
    public UserVO getLoginUser(String token) {
        Long userId = jwtUtils.getUserId(token);
        ThrowUtils.throwIf(userId == null, ErrorCode.NOT_LOGIN_ERROR, "Token无效或已过期");

        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf("DISABLED".equals(user.getStatus()),
                ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");

        return toUserVO(user);
    }

    @Override
    public boolean updateUser(User user) {
        ThrowUtils.throwIf(user.getId() == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        return this.updateById(user);
    }

    @Override
    public UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
