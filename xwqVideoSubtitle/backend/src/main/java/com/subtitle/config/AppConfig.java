package com.subtitle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties
@PropertySource("classpath:application.yml")
public class AppConfig {

    @Value("${app.video-path}")
    private String videoPath;

    @Value("${app.audio-path}")
    private String audioPath;

    @Value("${app.subtitle-path}")
    private String subtitlePath;

    @Value("${app.temp-path}")
    private String tempPath;

    @Value("${app.allowed-video-formats}")
    private String allowedVideoFormats;

    @Value("${app.max-video-size}")
    private Long maxVideoSize;

    // Getter方法
    public String getVideoPath() {
        return videoPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public String getSubtitlePath() {
        return subtitlePath;
    }

    public String getTempPath() {
        return tempPath;
    }

    public String getAllowedVideoFormats() {
        return allowedVideoFormats;
    }

    public Long getMaxVideoSize() {
        return maxVideoSize;
    }
}