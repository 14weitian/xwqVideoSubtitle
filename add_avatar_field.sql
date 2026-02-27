-- 添加用户头像字段
-- 执行时间: 2026-02-27
-- 描述: 为 users 表添加 avatar 字段用于存储用户头像路径

ALTER TABLE users
ADD COLUMN avatar VARCHAR(255) DEFAULT NULL COMMENT '用户头像路径'
AFTER email;

-- 验证字段是否添加成功
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = 'subtitle_db'
-- AND TABLE_NAME = 'users'
-- AND COLUMN_NAME = 'avatar';
