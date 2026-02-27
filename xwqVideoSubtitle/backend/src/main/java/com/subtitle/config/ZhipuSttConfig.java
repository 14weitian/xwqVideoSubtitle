package com.subtitle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * 智谱AI STT服务配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.stt.zhipu")
public class ZhipuSttConfig {

    /**
     * 智谱AI API密钥
     */
    private String apiKey;

    /**
     * API端点
     */
    private String endpoint = "https://open.bigmodel.cn/api/paas/v4/audio/transcriptions";

    /**
     * 使用的模型名称
     */
    private String model = "glm-asr";

    /**
     * 识别语言
     */
    private String language = "zh";

    /**
     * 请求超时时间(毫秒)
     */
    private Integer timeout = 60000;
}
