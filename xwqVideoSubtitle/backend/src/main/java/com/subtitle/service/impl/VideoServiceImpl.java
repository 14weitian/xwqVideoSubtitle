package com.subtitle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.subtitle.entity.Video;
import com.subtitle.dto.VideoUploadDTO;
import com.subtitle.mapper.VideoMapper;
import com.subtitle.service.VideoService;
import com.subtitle.utils.AudioExtractor;
import com.subtitle.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private AppConfig appConfig;

    @Override
    public Video uploadVideo(MultipartFile file, VideoUploadDTO uploadDTO, Long userId) {
        // 验证文件
        String validationResult = validateVideoFile(file);
        if (validationResult != null) {
            throw new RuntimeException(validationResult);
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        // 创建存储目录
        Path videoDir = Paths.get(appConfig.getVideoPath());
        if (!Files.exists(videoDir)) {
            try {
                Files.createDirectories(videoDir);
            } catch (IOException e) {
                throw new RuntimeException("创建视频存储目录失败", e);
            }
        }

        // 保存文件
        Path targetPath = videoDir.resolve(newFilename);
        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException("保存视频文件失败", e);
        }

        // 创建视频记录
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(StringUtils.hasText(uploadDTO.getTitle()) ? uploadDTO.getTitle() :
                     originalFilename.substring(0, originalFilename.lastIndexOf(".")));
        video.setFileName(originalFilename);
        video.setFilePath(targetPath.toString());
        video.setFileSize(file.getSize());
        video.setFormat(extension.substring(1));
        video.setStatus(1); // 上传完成
        video.setProgress(100);

        // 获取视频信息
        double duration = AudioExtractor.getVideoDuration(targetPath.toString());
        video.setDuration(String.format("%.2f", duration));

        // 保存到数据库
        videoMapper.insert(video);

        return video;
    }

    @Override
    public Video getVideoById(Long id) {
        return videoMapper.selectById(id);
    }

    @Override
    public List<Video> getAllVideos(Long userId) {
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Video::getUserId, userId);
        queryWrapper.orderByDesc(Video::getCreatedAt);
        return videoMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteVideo(Long id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            // 删除物理文件
            try {
                Files.deleteIfExists(Paths.get(video.getFilePath()));
            } catch (IOException e) {
                // 记录日志但继续删除数据库记录
                System.err.println("删除视频文件失败: " + e.getMessage());
            }

            // 删除数据库记录
            videoMapper.deleteById(id);
        }
    }

    @Override
    public boolean isVideoFormatSupported(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return false;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String allowedFormats = appConfig.getAllowedVideoFormats();

        return Arrays.asList(allowedFormats.split(",")).contains(extension);
    }

    @Override
    public String validateVideoFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return "文件不能为空";
        }

        // 检查文件大小
        if (file.getSize() > appConfig.getMaxVideoSize()) {
            return String.format("文件大小不能超过 %d MB", appConfig.getMaxVideoSize() / 1024 / 1024);
        }

        // 检查文件格式
        String originalFilename = file.getOriginalFilename();
        if (!isVideoFormatSupported(originalFilename)) {
            return "不支持的视频格式，支持的格式: " + appConfig.getAllowedVideoFormats();
        }

        return null;
    }
}