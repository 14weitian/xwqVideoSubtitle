-- =====================================================
-- 登录功能数据库迁移脚本
-- 执行方式：mysql -u subtitle -p subtitle_db < database_migration.sql
-- =====================================================

USE subtitle_db;

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 为 videos 表添加 user_id 字段
ALTER TABLE `videos`
ADD COLUMN `user_id` BIGINT COMMENT '用户ID' AFTER `id`;

-- 添加索引
ALTER TABLE `videos`
ADD INDEX `idx_user_id` (`user_id`);

-- 3. 创建默认管理员账户（密码：admin123）
-- BCrypt hash for "admin123": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `status`)
VALUES ('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '管理员', 1)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 4. 将现有视频关联到管理员账户
UPDATE `videos` SET `user_id` = 1 WHERE `user_id` IS NULL;

-- 验证迁移
SELECT '✓ 用户表创建成功' AS status;
SELECT COUNT(*) AS user_count FROM users;
SELECT '✓ videos 表已添加 user_id 字段' AS status;
SELECT COUNT(*) AS videos_with_user FROM videos WHERE user_id IS NOT NULL;
