package com.subtitle.dto;

import lombok.Data;

@Data
public class VideoUploadDTO {
    private String title;           // 视频标题
    private String language;        // 语言代码（默认zh-CN）
}