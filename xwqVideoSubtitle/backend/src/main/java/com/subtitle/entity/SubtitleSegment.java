package com.subtitle.entity;

import lombok.Data;

import java.util.List;

@Data
public class SubtitleSegment {
    private Integer index;           // 序号
    private Double startTime;        // 开始时间（秒）
    private Double endTime;          // 结束时间（秒）
    private String text;            // 字幕内容
    private Double confidence;       // 置信度（0-1）
    private Integer speaker;         // 说话人标识（多场景）
    private Double duration;        // 时长（秒）
    private List<String> alternatives; // 备选文本

    public void calculateDuration() {
        this.duration = endTime - startTime;
    }
}