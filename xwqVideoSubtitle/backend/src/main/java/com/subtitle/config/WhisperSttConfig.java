package com.subtitle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI Whisper STT服务配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.stt.whisper")
public class WhisperSttConfig {

    /**
     * OpenAI API密钥
     */
    private String apiKey;

    /**
     * API端点
     */
    private String endpoint = "https://api.openai.com/v1/audio/transcriptions";

    /**
     * 使用的模型名称
     */
    private String model = "whisper-1";

    /**
     * 请求超时时间(毫秒)
     */
    private Integer timeout = 120000; // 2分钟
}
