-- 创建邮箱验证码表
-- 执行时间: 2026-02-27
-- 描述: 创建用于存储邮箱验证码的表

CREATE TABLE IF NOT EXISTS email_verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    type VARCHAR(50) NOT NULL COMMENT '验证码类型（PASSWORD_RESET）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-未使用，2-已使用，0-已过期',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_email_type (email, type),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码表';

-- 验证表是否创建成功
-- SHOW CREATE TABLE email_verification_codes;
