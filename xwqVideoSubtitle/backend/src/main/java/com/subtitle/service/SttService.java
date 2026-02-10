package com.subtitle.service;

import com.subtitle.entity.SubtitleSegment;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public interface SttService {

    /**
     * 批量转写音频文件
     * @param audioPath 音频文件路径
     * @param language 语言代码
     * @return 字幕片段列表
     */
    List<SubtitleSegment> transcribeFile(String audioPath, String language);

    /**
     * 实时流式转写（预留）
     * @param audioStream 音频输入流
     * @param language 语言代码
     * @param callback 回调函数
     */
    void transcribeStream(InputStream audioStream, String language, Consumer<SubtitleSegment> callback);
}