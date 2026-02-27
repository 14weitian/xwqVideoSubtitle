package com.subtitle.controller;

import com.subtitle.entity.Video;
import com.subtitle.dto.VideoUploadDTO;
import com.subtitle.dto.ApiResponse;
import com.subtitle.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/videos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 上传视频
     */
    @PostMapping("/upload")
    public ApiResponse<Video> uploadVideo(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "title", required = false) String title,
                                         @RequestParam(value = "language", defaultValue = "zh-CN") String language,
                                         HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");

            VideoUploadDTO uploadDTO = new VideoUploadDTO();
            uploadDTO.setTitle(title);
            uploadDTO.setLanguage(language);

            Video video = videoService.uploadVideo(file, uploadDTO, userId);
            return ApiResponse.success(video, "视频上传成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "视频上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频列表
     */
    @GetMapping
    public ApiResponse<List<Video>> getAllVideos(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Video> videos = videoService.getAllVideos(userId);
            return ApiResponse.success(videos);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取视频列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Video> getVideo(@PathVariable Long id) {
        try {
            Video video = videoService.getVideoById(id);
            if (video == null) {
                return ApiResponse.error(404, "视频不存在");
            }
            return ApiResponse.success(video);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取视频失败: " + e.getMessage());
        }
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVideo(@PathVariable Long id) {
        try {
            videoService.deleteVideo(id);
            return ApiResponse.success(null, "视频删除成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "删除视频失败: " + e.getMessage());
        }
    }

    /**
     * 验证视频格式
     */
    @PostMapping("/validate")
    public ApiResponse<Boolean> validateVideoFormat(@RequestParam String filename) {
        try {
            boolean supported = videoService.isVideoFormatSupported(filename);
            return ApiResponse.success(supported, supported ? "格式支持" : "格式不支持");
        } catch (Exception e) {
            return ApiResponse.error(500, "验证失败: " + e.getMessage());
        }
    }
}