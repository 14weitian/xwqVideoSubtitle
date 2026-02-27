package com.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.subtitle.entity.EmailVerificationCode;
import com.subtitle.mapper.EmailVerificationCodeMapper;
import com.subtitle.service.EmailService;
import com.subtitle.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 验证码服务实现类
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private EmailVerificationCodeMapper codeMapper;

    @Autowired
    private EmailService emailService;

    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 10;

    @Override
    @Transactional
    public boolean sendCode(String email, String type) {
        // 生成6位验证码
        String code = emailService.generateCode();

        // 将之前的验证码标记为过期
        LambdaQueryWrapper<EmailVerificationCode> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getType, type)
                .eq(EmailVerificationCode::getStatus, 1);
        EmailVerificationCode oldCode = new EmailVerificationCode();
        oldCode.setStatus(0);  // 标记为过期
        codeMapper.update(oldCode, updateWrapper);

        // 保存新验证码
        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setStatus(1);  // 未使用
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        codeMapper.insert(verificationCode);

        // 发送邮件
        return emailService.sendVerificationCode(email, code, type);
    }

    @Override
    @Transactional
    public boolean verifyCode(String email, String code, String type) {
        // 查询验证码
        LambdaQueryWrapper<EmailVerificationCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getCode, code)
                .eq(EmailVerificationCode::getType, type)
                .eq(EmailVerificationCode::getStatus, 1)
                .gt(EmailVerificationCode::getExpiresAt, LocalDateTime.now())
                .orderByDesc(EmailVerificationCode::getCreatedAt)
                .last("LIMIT 1");

        EmailVerificationCode verificationCode = codeMapper.selectOne(queryWrapper);

        if (verificationCode == null) {
            return false;
        }

        // 标记验证码为已使用
        verificationCode.setStatus(2);
        codeMapper.updateById(verificationCode);

        return true;
    }
}
