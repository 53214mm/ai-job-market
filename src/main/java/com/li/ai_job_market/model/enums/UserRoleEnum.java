package com.li.ai_job_market.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    SEEKER("求职者", "SEEKER"),
    RECRUITER("招聘方", "RECRUITER"),
    ADMIN("管理员", "ADMIN");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有 value 列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(UserRoleEnum::getValue)
                .collect(Collectors.toList());
    }
}