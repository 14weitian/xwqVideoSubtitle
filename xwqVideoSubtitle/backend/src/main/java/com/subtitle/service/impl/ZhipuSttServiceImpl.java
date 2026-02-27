package com.subtitle.service.impl;

import com.subtitle.config.ZhipuSttConfig;
import com.subtitle.service.SttService;
import com.subtitle.entity.SubtitleSegment;
import com.subtitle.utils.AudioExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 智谱AI语音识别服务实现
 * 基于GLM-ASR模型实现语音转文字功能
 */
@Service
@ConditionalOnProperty(name = "app.stt.provider", havingValue = "zhipu")
public class ZhipuSttServiceImpl implements SttService {

    private static final Logger logger = LoggerFactory.getLogger(ZhipuSttServiceImpl.class);

    @Autowired
    private ZhipuSttConfig zhipuConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<SubtitleSegment> transcribeFile(String audioPath, String language) {
        logger.info("开始使用智谱AI GLM-ASR转写音频文件: {}", audioPath);
        logger.info("请求识别语言: {}", language);
        List<SubtitleSegment> segments = new ArrayList<>();

        try {
            // 检查音频时长
            double audioDuration = AudioExtractor.getAudioDuration(audioPath);
            logger.info("音频时长: {} 秒", audioDuration);

            // 智谱AI限制30秒，需要切片
            final int MAX_DURATION = 25; // 留5秒余量
            List<String> audioSegments;

            if (audioDuration > MAX_DURATION) {
                logger.info("音频时长超过{}秒，开始切片...", MAX_DURATION);
                audioSegments = AudioExtractor.splitAudioFile(audioPath, MAX_DURATION);
                logger.info("音频已切分为 {} 个片段", audioSegments.size());
            } else {
                logger.info("音频时长在限制内，无需切片");
                audioSegments = new ArrayList<>();
                audioSegments.add(audioPath);
            }

            // 依次识别每个片段
            double timeOffset = 0;
            for (int i = 0; i < audioSegments.size(); i++) {
                String segmentPath = audioSegments.get(i);
                logger.info("正在识别第 {}/{} 个片段: {}", i + 1, audioSegments.size(), segmentPath);

                List<SubtitleSegment> segmentResult = transcribeSingleFile(segmentPath, language);

                // 调整时间偏移
                for (SubtitleSegment segment : segmentResult) {
                    segment.setStartTime(segment.getStartTime() + timeOffset);
                    segment.setEndTime(segment.getEndTime() + timeOffset);
                    segments.add(segment);
                }

                // 更新时间偏移
                double segmentDuration = AudioExtractor.getAudioDuration(segmentPath);
                timeOffset += segmentDuration;

                logger.info("第 {} 个片段识别完成，获得 {} 个字幕片段", i + 1, segmentResult.size());
            }

            logger.info("所有片段识别完成，共 {} 个字幕片段", segments.size());

        } catch (Exception e) {
            logger.error("调用智谱AI API时发生错误", e);
        }

        return segments;
    }

    /**
     * 转写单个音频文件
     */
    private List<SubtitleSegment> transcribeSingleFile(String audioPath, String language) {
        List<SubtitleSegment> segments = new ArrayList<>();

        try {
            // 验证文件是否存在
            File audioFile = new File(audioPath);
            if (!audioFile.exists()) {
                logger.error("音频文件不存在: {}", audioPath);
                return segments;
            }

            // 构建请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(zhipuConfig.getApiKey());

            // 构建multipart请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audioFile));
            body.add("model", zhipuConfig.getModel());

            // 设置语言
            if ("auto".equalsIgnoreCase(language) || language == null || language.isEmpty()) {
                // 自动检测语言 - 不传递language参数，让智谱AI自动检测
                logger.info("启用自动语言检测模式");
                // 不添加language参数
            } else {
                // 使用指定的语言
                String apiLanguage = convertLanguageCode(language);
                body.add("language", apiLanguage);
                logger.info("使用指定语言代码: {} -> {}", language, apiLanguage);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            logger.debug("调用智谱AI API: {}", zhipuConfig.getEndpoint());

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    zhipuConfig.getEndpoint(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("智谱AI转写成功");
                segments = parseTranscriptionResponse(response.getBody());
                logger.info("解析到 {} 个字幕片段", segments.size());
            } else {
                logger.error("智谱AI转写失败: HTTP {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("转写单个音频文件失败: " + audioPath, e);
        }

        return segments;
    }

    @Override
    public void transcribeStream(InputStream audioStream, String language, Consumer<SubtitleSegment> callback) {
        logger.info("开始使用智谱AI实时转写音频流");
        // TODO: 实现实时流式转写(智谱AI可能需要通过WebSocket实现)
        logger.warn("智谱AI实时流式转写功能暂未实现");
    }

    /**
     * 解析智谱AI转写响应
     * @param jsonResponse JSON响应字符串
     * @return 字幕片段列表
     */
    private List<SubtitleSegment> parseTranscriptionResponse(String jsonResponse) {
        List<SubtitleSegment> segments = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // 检查是否有segments字段(带时间戳的响应)
            if (root.has("segments")) {
                JsonNode segmentsNode = root.get("segments");
                if (segmentsNode.isArray()) {
                    for (JsonNode segmentNode : segmentsNode) {
                        SubtitleSegment segment = new SubtitleSegment();
                        segment.setText(segmentNode.get("text").asText());

                        // 解析时间戳（单位：秒）
                        if (segmentNode.has("start")) {
                            segment.setStartTime(segmentNode.get("start").asDouble());
                        }
                        if (segmentNode.has("end")) {
                            segment.setEndTime(segmentNode.get("end").asDouble());
                        }

                        segments.add(segment);
                    }
                }
            } else if (root.has("text")) {
                // 如果只有纯文本,创建单个字幕片段
                String fullText = root.get("text").asText();
                SubtitleSegment segment = new SubtitleSegment();
                segment.setText(fullText);
                segment.setStartTime(0.0);
                // 默认设置为10秒(如果无法获取准确时长)
                segment.setEndTime(10.0);
                segments.add(segment);
            }

        } catch (Exception e) {
            logger.error("解析智谱AI响应失败", e);
        }

        return segments;
    }

    /**
     * 转换语言代码为智谱AI支持的格式
     * @param language 输入的语言代码（如 zh-CN, en-US）
     * @return 智谱AI支持的语言代码（如 zh, en）
     */
    private String convertLanguageCode(String language) {
        if (language == null || language.isEmpty()) {
            return zhipuConfig.getLanguage(); // 使用配置的默认语言
        }

        // 智谱AI支持的语言代码（根据官方文档）
        // 返回基础语言代码（去掉地区部分）
        if (language.contains("-")) {
            return language.split("-")[0].toLowerCase();
        }

        // 常见语言代码映射
        switch (language.toLowerCase()) {
            case "zh-cn":
            case "zh-tw":
            case "zh-hk":
                return "zh";
            case "en-us":
            case "en-gb":
            case "en-au":
            case "en-ca":
                return "en";
            case "ja-jp":
                return "ja";
            case "ko-kr":
                return "ko";
            case "fr-fr":
            case "fr-ca":
                return "fr";
            case "de-de":
            case "de-at":
                return "de";
            case "es-es":
            case "es-mx":
                return "es";
            case "ru-ru":
                return "ru";
            case "pt-br":
            case "pt-pt":
                return "pt";
            case "it-it":
                return "it";
            default:
                // 如果无法识别，返回原始代码或默认语言
                return language.toLowerCase();
        }
    }
}
