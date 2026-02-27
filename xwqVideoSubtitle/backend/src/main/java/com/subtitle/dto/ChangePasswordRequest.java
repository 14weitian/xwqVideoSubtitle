package com.subtitle.dto;

import lombok.Data;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordRequest {
    private String oldPassword;      // 旧密码
    private String newPassword;      // 新密码
    private String confirmPassword;  // 确认新密码
}
