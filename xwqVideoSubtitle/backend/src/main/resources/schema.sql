-- 创建数据库
CREATE DATABASE IF NOT EXISTS subtitle_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE subtitle_db;

-- 视频表
CREATE TABLE videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    duration VARCHAR(20),
    format VARCHAR(20),
    status TINYINT DEFAULT 0 COMMENT '0: 上传中, 1: 完成, 2: 处理中, 3: 失败',
    progress INT DEFAULT 0 COMMENT '处理进度百分比',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 字幕表
CREATE TABLE subtitles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL COMMENT '语言代码：zh-CN, en-US等',
    content JSON NOT NULL COMMENT '字幕片段数组',
    format VARCHAR(10) DEFAULT 'JSON' COMMENT 'JSON, SRT, VTT',
    status TINYINT DEFAULT 0 COMMENT '0: 处理中, 1: 完成, 2: 失败',
    error_message TEXT,
    duration INT COMMENT '总时长（秒）',
    segment_count INT,
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
);

-- 字幕表索引
CREATE INDEX idx_video_id ON subtitles(video_id);
CREATE INDEX idx_language ON subtitles(language);
CREATE INDEX idx_status ON subtitles(status);
CREATE INDEX idx_created_at ON subtitles(created_at);

-- 创建任务记录表（用于异步任务跟踪）
CREATE TABLE task_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(100) NOT NULL UNIQUE,
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：subtitle_generate, audio_extract等',
    video_id BIGINT,
    status TINYINT DEFAULT 0 COMMENT '0: 进行中, 1: 完成, 2: 失败',
    progress INT DEFAULT 0,
    message TEXT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入基础数据
INSERT INTO task_records (task_id, task_type, status) VALUES ('init', 'system', 1);