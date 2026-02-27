package com.subtitle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.subtitle.entity.EmailVerificationCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱验证码 Mapper
 */
@Mapper
public interface EmailVerificationCodeMapper extends BaseMapper<EmailVerificationCode> {
}
