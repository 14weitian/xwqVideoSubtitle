package com.subtitle.service;

import com.subtitle.entity.Subtitle;
import com.subtitle.entity.Video;
import com.subtitle.dto.SubtitleGenerateDTO;
import com.subtitle.entity.SubtitleSegment;
import com.subtitle.entity.TaskRecord;
import com.subtitle.mapper.SubtitleMapper;
import com.subtitle.mapper.VideoMapper;
import com.subtitle.mapper.TaskRecordMapper;
import com.subtitle.service.SttService;
import com.subtitle.utils.AudioExtractor;
import com.subtitle.utils.SubtitleFormatConverter;
import com.subtitle.dto.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SubtitleService {

    private static final Logger logger = LoggerFactory.getLogger(SubtitleService.class);

    @Autowired
    private SubtitleMapper subtitleMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private TaskRecordMapper taskRecordMapper;

    @Autowired
    private SttService sttService;

    /**
     * 异步生成字幕
     */
    @Async
    public CompletableFuture<ApiResponse<Subtitle>> generateSubtitleAsync(String taskId, Long videoId, SubtitleGenerateDTO generateDTO) {
        try {
            // 获取视频信息
            Video video = videoMapper.selectById(videoId);
            if (video == null) {
                return CompletableFuture.completedFuture(ApiResponse.error("视频不存在"));
            }

            // 创建任务记录
            TaskRecord task = new TaskRecord();
            task.setTaskId(taskId);
            task.setTaskType("subtitle_generate");
            task.setVideoId(videoId);
            task.setStatus(0); // 进行中
            task.setProgress(0);
            task.setMessage("开始生成字幕");
            taskRecordMapper.insert(task);

            // 执行字幕生成流程
            Subtitle subtitle = generateSubtitle(video, generateDTO, task);

            if (subtitle != null) {
                return CompletableFuture.completedFuture(ApiResponse.success(subtitle));
            } else {
                task.setStatus(2); // 失败
                task.setErrorMessage("字幕生成失败");
                taskRecordMapper.updateById(task);
                return CompletableFuture.completedFuture(ApiResponse.error("字幕生成失败"));
            }

        } catch (Exception e) {
            // 更新任务状态为失败
            updateTaskStatus(taskId, 2, null, e.getMessage());
            return CompletableFuture.completedFuture(ApiResponse.error("字幕生成失败: " + e.getMessage()));
        }
    }

    /**
     * 生成字幕（同步方法）
     */
    public Subtitle generateSubtitle(Video video, SubtitleGenerateDTO generateDTO, TaskRecord task) {
        try {
            // 1. 提取音频
            updateTaskStatus(task.getTaskId(), 0, 10, "开始提取音频");
            String audioPath = extractAudio(video);

            if (audioPath == null) {
                throw new RuntimeException("音频提取失败");
            }

            // 2. 调用STT服务
            updateTaskStatus(task.getTaskId(), 0, 30, "开始语音识别");
            List<SubtitleSegment> segments = sttService.transcribeFile(audioPath, generateDTO.getLanguage());

            if (segments.isEmpty()) {
                throw new RuntimeException("语音识别结果为空");
            }

            // 3. 保存字幕
            updateTaskStatus(task.getTaskId(), 0, 80, "保存字幕数据");
            Subtitle subtitle = saveSubtitle(video, segments, generateDTO);

            // 4. 生成字幕文件
            updateTaskStatus(task.getTaskId(), 0, 95, "生成字幕文件");
            generateSubtitleFile(subtitle, segments);

            // 更新任务状态
            updateTaskStatus(task.getTaskId(), 1, 100, "字幕生成完成");

            return subtitle;

        } catch (Exception e) {
            updateTaskStatus(task.getTaskId(), 2, 0, "字幕生成失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 提取音频
     */
    private String extractAudio(Video video) {
        logger.info("开始提取音频 - 视频ID: {}, 视频路径: {}", video.getId(), video.getFilePath());

        String audioFilename = video.getId() + ".wav";
        String audioPath = Paths.get(getAppConfig().getAudioPath(), audioFilename).toString();

        logger.info("音频输出路径: {}", audioPath);

        // 确保音频目录存在
        File audioDir = new File(getAppConfig().getAudioPath());
        if (!audioDir.exists()) {
            boolean created = audioDir.mkdirs();
            logger.info("创建音频目录: {}, 结果: {}", audioDir.getAbsolutePath(), created);
        }

        // 检查视频文件是否存在
        File videoFile = new File(video.getFilePath());
        if (!videoFile.exists()) {
            logger.error("视频文件不存在: {}", video.getFilePath());
            throw new RuntimeException("视频文件不存在: " + video.getFilePath());
        }

        logger.info("视频文件大小: {} bytes", videoFile.length());

        // 检查视频是否包含音频
        if (!AudioExtractor.hasAudio(video.getFilePath())) {
            logger.error("视频文件不包含音频流: {}", video.getFilePath());
            throw new RuntimeException("视频文件不包含音频流");
        }

        // 提取音频
        boolean success = AudioExtractor.extractAudio(video.getFilePath(), audioPath);
        if (success) {
            logger.info("音频提取成功: {}", audioPath);
            return audioPath;
        } else {
            logger.error("音频提取失败");
            throw new RuntimeException("音频提取失败");
        }
    }

    /**
     * 保存字幕到数据库
     */
    private Subtitle saveSubtitle(Video video, List<SubtitleSegment> segments, SubtitleGenerateDTO generateDTO) {
        try {
            Subtitle subtitle = new Subtitle();
            subtitle.setVideoId(video.getId());
            subtitle.setLanguage(generateDTO.getLanguage());
            subtitle.setContent(objectMapper.writeValueAsString(segments));
            subtitle.setFormat(generateDTO.getFormat());
            subtitle.setStatus(1); // 完成
            subtitle.setDuration(calculateTotalDuration(segments));
            subtitle.setSegmentCount(segments.size());
            subtitle.setFilePath(""); // 暂时为空，后续生成文件时更新

            subtitleMapper.insert(subtitle);
            return subtitle;

        } catch (Exception e) {
            throw new RuntimeException("保存字幕失败", e);
        }
    }

    /**
     * 生成字幕文件
     */
    private void generateSubtitleFile(Subtitle subtitle, List<SubtitleSegment> segments) throws Exception {
        String content;
        switch (subtitle.getFormat().toUpperCase()) {
            case "SRT":
                content = SubtitleFormatConverter.convertToSrt(segments);
                break;
            case "VTT":
                content = SubtitleFormatConverter.convertToVtt(segments);
                break;
            default:
                content = SubtitleFormatConverter.convertToSrt(segments);
        }

        String filename = subtitle.getId() + "." + subtitle.getFormat().toLowerCase();
        String filePath = Paths.get(getAppConfig().getSubtitlePath(), filename).toString();

        // 确保字幕目录存在
        File subtitleDir = new File(getAppConfig().getSubtitlePath());
        if (!subtitleDir.exists()) {
            subtitleDir.mkdirs();
        }

        // 保存字幕文件
        SubtitleFormatConverter.saveSubtitleFile(content, filePath);

        // 更新字幕记录中的文件路径
        subtitle.setFilePath(filePath);
        subtitleMapper.updateById(subtitle);
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, int status, Integer progress, String message) {
        TaskRecord task = taskRecordMapper.selectByMap(
                Collections.singletonMap("task_id", taskId)).stream().findFirst().orElse(null);
        if (task != null) {
            task.setStatus(status);
            if (progress != null) {
                task.setProgress(progress);
            }
            task.setMessage(message);

            // 如果是失败状态，同时设置errorMessage
            if (status == 2) {
                task.setErrorMessage(message);
            }

            taskRecordMapper.updateById(task);
        }
    }

    /**
     * 计算总时长
     */
    private int calculateTotalDuration(List<SubtitleSegment> segments) {
        return segments.stream()
                .mapToInt(segment -> (int) Math.round(segment.getEndTime()))
                .max()
                .orElse(0);
    }

    /**
     * 获取任务状态
     */
    public TaskRecord getTaskStatus(String taskId) {
        return taskRecordMapper.selectByMap(
                Collections.singletonMap("task_id", taskId)).stream().findFirst().orElse(null);
    }

    /**
     * 获取视频的所有字幕
     */
    public List<Subtitle> getSubtitlesByVideoId(Long videoId) {
        return subtitleMapper.selectList(
                new LambdaQueryWrapper<Subtitle>()
                        .eq(Subtitle::getVideoId, videoId)
        );
    }

    /**
     * 获取字幕文件
     */
    public byte[] getSubtitleFile(Long subtitleId) {
        Subtitle subtitle = subtitleMapper.selectById(subtitleId);
        if (subtitle != null && StringUtils.hasText(subtitle.getFilePath())) {
            try {
                return Files.readAllBytes(Paths.get(subtitle.getFilePath()));
            } catch (IOException e) {
                throw new RuntimeException("读取字幕文件失败", e);
            }
        }
        return null;
    }

    // 获取应用配置的辅助方法
    private com.subtitle.config.AppConfig getAppConfig() {
        return applicationContext.getBean(com.subtitle.config.AppConfig.class);
    }

    // 注入applicationContext
    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    // 注入objectMapper
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
}