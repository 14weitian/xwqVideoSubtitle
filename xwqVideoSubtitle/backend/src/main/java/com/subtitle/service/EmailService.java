package com.subtitle.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @param type 类型（PASSWORD_RESET）
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String to, String code, String type);

    /**
     * 生成6位随机验证码
     * @return 验证码
     */
    String generateCode();
}
