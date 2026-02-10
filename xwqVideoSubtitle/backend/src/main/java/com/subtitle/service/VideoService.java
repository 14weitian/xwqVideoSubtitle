package com.subtitle.service;

import com.subtitle.entity.Video;
import com.subtitle.dto.VideoUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    /**
     * 上传视频文件
     * @param file 视频文件
     * @param uploadDTO 上传参数
     * @return 视频信息
     */
    Video uploadVideo(MultipartFile file, VideoUploadDTO uploadDTO);

    /**
     * 根据ID获取视频
     * @param id 视频ID
     * @return 视频信息
     */
    Video getVideoById(Long id);

    /**
     * 获取所有视频
     * @return 视频列表
     */
    List<Video> getAllVideos();

    /**
     * 删除视频
     * @param id 视频ID
     */
    void deleteVideo(Long id);

    /**
     * 检查视频格式是否支持
     * @param originalFilename 文件名
     * @return 是否支持
     */
    boolean isVideoFormatSupported(String originalFilename);

    /**
     * 验证视频文件
     * @param file 视频文件
     * @return 验证结果
     */
    String validateVideoFile(MultipartFile file);
}