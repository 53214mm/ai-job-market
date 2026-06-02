package com.li.ai_job_market.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件服务 —— 通过QQ邮箱SMTP发送验证码，内存存储验证码缓存
 */
@Slf4j
@Service
public class EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /** 内存缓存：邮箱 → 验证码（生产环境应使用Redis） */
    private final Map<String, CodeEntry> codeCache = new ConcurrentHashMap<>();

    private static final long CODE_TTL_MS = 5 * 60 * 1000; // 5分钟有效期

    /** 发送验证码邮件 */
    public void sendVerificationCode(String toEmail) {
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        codeCache.put(toEmail, new CodeEntry(code, System.currentTimeMillis()));

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("AI求职市场 - 邮箱验证码");
            helper.setText(buildEmailContent(code), true);
            mailSender.send(msg);
            log.info("验证码已发送至 {}", toEmail);
        } catch (MessagingException e) {
            codeCache.remove(toEmail);
            log.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /** 校验验证码，校验成功后删除缓存 */
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeCache.get(email);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.timestamp > CODE_TTL_MS) {
            codeCache.remove(email);
            return false;
        }
        if (entry.code.equals(code)) {
            codeCache.remove(email);
            return true;
        }
        return false;
    }

    private String buildEmailContent(String code) {
        return """
            <div style="max-width:480px;margin:0 auto;padding:24px;font-family:Arial,sans-serif;
                 background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px">
              <h2 style="color:#1d4ed8;text-align:center">AI 求职市场</h2>
              <p style="font-size:16px;color:#334155">您的邮箱验证码：</p>
              <div style="text-align:center;margin:20px 0">
                <span style="font-size:32px;font-weight:bold;color:#1d4ed8;letter-spacing:6px;
                     background:#eff6ff;padding:12px 24px;border-radius:8px">%s</span>
              </div>
              <p style="font-size:13px;color:#94a3b8">验证码5分钟内有效，请勿泄露给他人。</p>
            </div>
            """.formatted(code);
    }

    private record CodeEntry(String code, long timestamp) {}
}
