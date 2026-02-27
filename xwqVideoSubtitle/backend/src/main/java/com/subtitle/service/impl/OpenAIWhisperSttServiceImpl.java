package com.subtitle.service.impl;

import com.subtitle.service.SttService;
import com.subtitle.entity.SubtitleSegment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * OpenAI Whisper语音识别服务实现
 * 支持长音频，一次调用即可完成
 */
@Service
@ConditionalOnProperty(name = "app.stt.provider", havingValue = "whisper")
public class OpenAIWhisperSttServiceImpl implements SttService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIWhisperSttServiceImpl.class);

    @Value("${app.stt.whisper.api-key}")
    private String apiKey;

    @Value("${app.stt.whisper.endpoint:https://api.openai.com/v1/audio/transcriptions}")
    private String endpoint;

    @Value("${app.stt.whisper.model:whisper-1}")
    private String model;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<SubtitleSegment> transcribeFile(String audioPath, String language) {
        logger.info("开始使用OpenAI Whisper转写音频文件: {}", audioPath);
        logger.info("请求识别语言: {}", language);
        List<SubtitleSegment> segments = new ArrayList<>();

        try {
            // 验证文件是否存在
            File audioFile = new File(audioPath);
            if (!audioFile.exists()) {
                logger.error("音频文件不存在: {}", audioPath);
                return segments;
            }

            long fileSize = audioFile.length();
            logger.info("音频文件大小: {} bytes ({} MB)", fileSize, fileSize / 1024 / 1024);

            // 检查文件大小限制（25MB）
            if (fileSize > 25 * 1024 * 1024) {
                logger.error("音频文件太大: {} MB，超过25MB限制", fileSize / 1024 / 1024);
                throw new RuntimeException("音频文件太大，最大支持25MB");
            }

            // 构建请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            // 构建multipart请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audioFile));
            body.add("model", model);

            // 设置语言（auto或不指定则自动检测）
            if (!"auto".equalsIgnoreCase(language) && language != null && !language.isEmpty()) {
                // 转换语言代码
                String whisperLanguage = convertToWhisperLanguage(language);
                body.add("language", whisperLanguage);
                logger.info("使用指定语言: {} -> {}", language, whisperLanguage);
            } else {
                logger.info("启用自动语言检测");
            }

            // 请求带时间戳的响应格式
            body.add("response_format", "verbose_json");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            logger.info("调用OpenAI Whisper API: {}", endpoint);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Whisper转写成功");
                segments = parseWhisperResponse(response.getBody());
                logger.info("解析到 {} 个字幕片段", segments.size());
            } else {
                logger.error("Whisper转写失败: HTTP {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("调用OpenAI Whisper API时发生错误", e);
            throw new RuntimeException("语音识别失败: " + e.getMessage(), e);
        }

        return segments;
    }

    @Override
    public void transcribeStream(InputStream audioStream, String language, Consumer<SubtitleSegment> callback) {
        logger.info("开始使用Whisper实时转写音频流");
        logger.warn("Whisper暂不支持实时流式转写");
    }

    /**
     * 解析Whisper API响应
     */
    private List<SubtitleSegment> parseWhisperResponse(String jsonResponse) {
        List<SubtitleSegment> segments = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // Whisper verbose_json格式包含segments数组
            if (root.has("segments")) {
                JsonNode segmentsNode = root.get("segments");
                if (segmentsNode.isArray()) {
                    for (JsonNode segmentNode : segmentsNode) {
                        SubtitleSegment segment = new SubtitleSegment();
                        segment.setText(segmentNode.get("text").asText().trim());

                        // 解析时间戳（秒）
                        if (segmentNode.has("start")) {
                            segment.setStartTime(segmentNode.get("start").asDouble());
                        }
                        if (segmentNode.has("end")) {
                            segment.setEndTime(segmentNode.get("end").asDouble());
                        }

                        // 只有非空文本才添加
                        if (!segment.getText().isEmpty()) {
                            segments.add(segment);
                        }
                    }
                }
            } else if (root.has("text")) {
                // 如果只有纯文本（非verbose格式），创建单个片段
                String fullText = root.get("text").asText();
                SubtitleSegment segment = new SubtitleSegment();
                segment.setText(fullText);
                segment.setStartTime(0.0);
                segment.setEndTime(0.0);
                segments.add(segment);
            }

        } catch (Exception e) {
            logger.error("解析Whisper响应失败", e);
        }

        return segments;
    }

    /**
     * 转换语言代码为Whisper格式
     */
    private String convertToWhisperLanguage(String language) {
        // Whisper使用ISO 639-1代码
        if (language.contains("-")) {
            return language.split("-")[0].toLowerCase();
        }

        switch (language.toLowerCase()) {
            case "zh-cn":
            case "zh-tw":
                return "zh";
            case "en-us":
            case "en-gb":
                return "en";
            case "ja-jp":
                return "ja";
            case "ko-kr":
                return "ko";
            default:
                return language.toLowerCase();
        }
    }
}
