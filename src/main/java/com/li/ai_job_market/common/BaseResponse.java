package com.li.ai_job_market.common;

import com.li.ai_job_market.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应封装类，统一 API 返回结构
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, String message) {
        this(code, message, null);
    }

    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
