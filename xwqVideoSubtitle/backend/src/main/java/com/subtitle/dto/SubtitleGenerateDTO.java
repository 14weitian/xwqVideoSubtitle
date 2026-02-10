package com.subtitle.dto;

import lombok.Data;

@Data
public class SubtitleGenerateDTO {
    private Long videoId;           // 视频ID
    private String language;        // 目标语言
    private String format;          // 输出格式（JSON, SRT, VTT）
}