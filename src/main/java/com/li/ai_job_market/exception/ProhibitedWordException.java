package com.li.ai_job_market.exception;

/**
 * 敏感词异常，在检测到违禁词时抛出
 */
public class ProhibitedWordException extends RuntimeException {
    public ProhibitedWordException(String message) {
        super(message);
    }

    public ProhibitedWordException(String message, Throwable cause) {
        super(message, cause);
    }
}

