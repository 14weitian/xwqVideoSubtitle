package com.subtitle.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param email 邮箱地址
     * @param type 类型（PASSWORD_RESET）
     * @return 是否发送成功
     */
    boolean sendCode(String email, String type);

    /**
     * 验证验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 类型
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code, String type);
}
