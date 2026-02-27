package com.subtitle.controller;

import com.subtitle.entity.Subtitle;
import com.subtitle.entity.Video;
import com.subtitle.dto.SubtitleGenerateDTO;
import com.subtitle.dto.ApiResponse;
import com.subtitle.service.SubtitleService;
import com.subtitle.service.VideoService;
import com.subtitle.mapper.SubtitleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/subtitles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SubtitleController {

    @Autowired
    private SubtitleMapper subtitleMapper;

    @Autowired
    private SubtitleService subtitleService;

    @Autowired
    private VideoService videoService;

    /**
     * 生成字幕
     */
    @PostMapping("/generate")
    public ApiResponse<String> generateSubtitle(@RequestBody SubtitleGenerateDTO generateDTO) {
        try {
            // 验证视频是否存在
            Video video = videoService.getVideoById(generateDTO.getVideoId());
            if (video == null) {
                return ApiResponse.error(404, "视频不存在");
            }

            // 生成任务ID
            String taskId = "subtitle_" + System.currentTimeMillis();

            // 异步生成字幕，传入taskId
            subtitleService.generateSubtitleAsync(taskId, generateDTO.getVideoId(), generateDTO);

            // 返回任务ID供客户端查询进度
            return ApiResponse.success(taskId, "字幕生成任务已启动，请使用任务ID查询进度");
        } catch (Exception e) {
            return ApiResponse.error(500, "启动字幕生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/task/{taskId}")
    public ApiResponse<?> getTaskStatus(@PathVariable String taskId) {
        try {
            Object taskStatus = subtitleService.getTaskStatus(taskId);
            if (taskStatus == null) {
                return ApiResponse.error(404, "任务不存在");
            }
            return ApiResponse.success(taskStatus);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频的所有字幕
     */
    @GetMapping("/video/{videoId}")
    public ApiResponse<List<Subtitle>> getSubtitlesByVideoId(@PathVariable Long videoId) {
        try {
            List<Subtitle> subtitles = subtitleService.getSubtitlesByVideoId(videoId);
            return ApiResponse.success(subtitles);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取字幕列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取字幕详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Subtitle> getSubtitle(@PathVariable Long id) {
        try {
            Subtitle subtitle = subtitleMapper.selectById(id);
            if (subtitle == null) {
                return ApiResponse.error(404, "字幕不存在");
            }
            return ApiResponse.success(subtitle);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取字幕失败: " + e.getMessage());
        }
    }

    /**
     * 导出字幕文件
     */
    @GetMapping("/{id}/export")
    public ApiResponse<?> exportSubtitle(@PathVariable Long id,
                                       @RequestParam(defaultValue = "srt") String format) {
        try {
            byte[] subtitleFile = subtitleService.getSubtitleFile(id);
            if (subtitleFile == null) {
                return ApiResponse.error(404, "字幕文件不存在");
            }
            return ApiResponse.success(subtitleFile, "字幕导出成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "导出字幕失败: " + e.getMessage());
        }
    }

    /**
     * 删除字幕
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSubtitle(@PathVariable Long id) {
        try {
            // 获取字幕信息
            Subtitle subtitle = subtitleMapper.selectById(id);
            if (subtitle == null) {
                return ApiResponse.error(404, "字幕不存在");
            }

            // 删除物理文件
            if (subtitle.getFilePath() != null) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(subtitle.getFilePath()));
                } catch (Exception e) {
                    // 记录日志但继续删除数据库记录
                    System.err.println("删除字幕文件失败: " + e.getMessage());
                }
            }

            // 删除数据库记录
            subtitleMapper.deleteById(id);
            return ApiResponse.success(null, "字幕删除成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "删除字幕失败: " + e.getMessage());
        }
    }
}