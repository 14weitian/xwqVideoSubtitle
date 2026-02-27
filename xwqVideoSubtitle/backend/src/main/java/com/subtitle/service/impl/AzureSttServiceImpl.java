package com.subtitle.service.impl;

import com.subtitle.service.SttService;
import com.subtitle.entity.SubtitleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(name = "app.stt.provider", havingValue = "azure")
public class AzureSttServiceImpl implements SttService {

    private static final Logger logger = LoggerFactory.getLogger(AzureSttServiceImpl.class);

    @Override
    public List<SubtitleSegment> transcribeFile(String audioPath, String language) {
        logger.info("开始使用Azure Speech服务转写音频文件: {}", audioPath);
        List<SubtitleSegment> segments = new ArrayList<>();

        // TODO: 实现Azure Speech API调用
        // 示例实现：
        // 1. 创建Azure Speech配置
        // 2. 调用Speech to Text API
        // 3. 解析返回的字幕数据
        // 4. 转换为SubtitleSegment对象

        logger.warn("Azure STT服务暂未实现，请配置API密钥后重试");
        return segments;
    }

    @Override
    public void transcribeStream(InputStream audioStream, String language, Consumer<SubtitleSegment> callback) {
        logger.info("开始使用Azure实时转写音频流");
        // TODO: 实现实时流式转写
    }
}