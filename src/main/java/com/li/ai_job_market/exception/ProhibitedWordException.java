package com.li.ai_job_market.exception;

public class ProhibitedWordException extends RuntimeException {
    public ProhibitedWordException(String message) {
        super(message);
    }

    public ProhibitedWordException(String message, Throwable cause) {
        super(message, cause);
    }
}

