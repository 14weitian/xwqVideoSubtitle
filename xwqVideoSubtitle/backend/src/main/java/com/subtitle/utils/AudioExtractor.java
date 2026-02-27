package com.subtitle.utils;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AudioExtractor.class);

    /**
     * 从视频中提取音频（使用命令行FFmpeg，更稳定）
     * @param videoPath 视频文件路径
     * @param audioPath 输出的音频文件路径
     * @return 是否提取成功
     */
    public static boolean extractAudioWithCommandLine(String videoPath, String audioPath) {
        logger.info("使用命令行FFmpeg提取音频: {} -> {}", videoPath, audioPath);

        try {
            // 确保输出目录存在
            File audioFile = new File(audioPath);
            File parentDir = audioFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                logger.info("创建输出目录: {}", parentDir.getAbsolutePath());
            }

            // 构建FFmpeg命令
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",  // 覆盖输出文件
                "-i", videoPath,  // 输入文件
                "-vn",  // 不包含视频
                "-acodec", "pcm_s16le",  // 音频编码器
                "-ar", "16000",  // 采样率
                "-ac", "1",  // 单声道
                audioPath  // 输出文件
            );

            pb.redirectErrorStream(true);

            logger.info("执行命令: ffmpeg -i {} -vn -acodec pcm_s16le -ar 16000 -ac 1 {}",
                videoPath, audioPath);

            Process process = pb.start();

            // 读取输出
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("FFmpeg: {}", line);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                File output = new File(audioPath);
                if (output.exists() && output.length() > 0) {
                    logger.info("音频提取成功: {}, 文件大小: {} bytes", audioPath, output.length());
                    return true;
                } else {
                    logger.error("音频文件未生成: {}", audioPath);
                    return false;
                }
            } else {
                logger.error("FFmpeg命令执行失败，退出码: {}", exitCode);
                return false;
            }

        } catch (Exception e) {
            logger.error("命令行音频提取失败", e);
            return false;
        }
    }

    /**
     * 从视频中提取音频
     * @param videoPath 视频文件路径
     * @param audioPath 输出的音频文件路径
     * @return 是否提取成功
     */
    public static boolean extractAudio(String videoPath, String audioPath) {
        logger.info("开始提取音频: 视频路径={}, 音频路径={}", videoPath, audioPath);

        // 检查视频文件是否存在
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            logger.error("视频文件不存在: {}", videoPath);
            return false;
        }

        // 检查视频文件大小
        if (videoFile.length() == 0) {
            logger.error("视频文件为空: {}", videoPath);
            return false;
        }

        // 优先使用命令行方式（更稳定）
        if (isFFmpegAvailable()) {
            logger.info("使用命令行FFmpeg方式提取音频");
            return extractAudioWithCommandLine(videoPath, audioPath);
        }

        // 降级到JavaCV方式
        logger.info("使用JavaCV方式提取音频");
        return extractAudioWithJavaCV(videoPath, audioPath);
    }

    /**
     * 使用JavaCV提取音频
     */
    private static boolean extractAudioWithJavaCV(String videoPath, String audioPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {

            // 设置音频格式
            grabber.setAudioChannels(1);
            grabber.setSampleRate(16000);

            logger.info("正在初始化FFmpeg grabber...");
            grabber.start();

            // 检查是否有音频流
            if (grabber.getAudioStream() == -1) {
                logger.error("视频中未找到音频流: {}", videoPath);
                grabber.stop();
                return false;
            }

            logger.info("找到音频流，开始提取音频...");

            // 确保输出目录存在
            File audioFile = new File(audioPath);
            File parentDir = audioFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                logger.info("创建输出目录: {}", parentDir.getAbsolutePath());
            }

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(audioPath, 1, 16000)) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_PCM_S16LE);
                recorder.setFormat("wav");
                recorder.setAudioChannels(1);
                recorder.setSampleRate(16000);

                logger.info("正在初始化FFmpeg recorder...");
                recorder.start();

                Frame frame;
                int frameCount = 0;
                while ((frame = grabber.grab()) != null) {
                    if (frame.samples != null) {
                        recorder.record(frame);
                        frameCount++;
                        if (frameCount % 1000 == 0) {
                            logger.debug("已处理 {} 个音频帧", frameCount);
                        }
                    }
                }
                recorder.stop();
                logger.info("音频提取完成，共处理 {} 个音频帧", frameCount);
            }

            grabber.stop();

            // 验证输出文件
            File outputFile = new File(audioPath);
            if (outputFile.exists() && outputFile.length() > 0) {
                logger.info("音频提取成功: {}, 文件大小: {} bytes", audioPath, outputFile.length());
                return true;
            } else {
                logger.error("音频文件生成失败: {}", audioPath);
                return false;
            }

        } catch (IOException e) {
            logger.error("音频提取失败: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("音频提取发生未知错误: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查系统是否安装了FFmpeg
     */
    private static boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.warn("系统未安装FFmpeg命令行工具");
            return false;
        }
    }

    /**
     * 获取视频信息
     * @param videoPath 视频文件路径
     * @return 视频时长（秒）
     */
    public static double getVideoDuration(String videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            double duration = grabber.getLengthInTime() / (double) 1000000; // 转换为秒
            grabber.stop();
            return duration;
        } catch (IOException e) {
            logger.error("获取视频时长失败", e);
            return 0;
        }
    }

    /**
     * 检查视频是否包含音频
     * @param videoPath 视频文件路径
     * @return 是否包含音频
     */
    public static boolean hasAudio(String videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            boolean hasAudio = grabber.getAudioStream() != -1;
            grabber.stop();
            return hasAudio;
        } catch (IOException e) {
            logger.error("检查音频失败", e);
            return false;
        }
    }

    /**
     * 获取视频格式
     * @param videoPath 视频文件路径
     * @return 格式名称
     */
    public static String getVideoFormat(String videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            String format = grabber.getFormat();
            grabber.stop();
            return format;
        } catch (IOException e) {
            logger.error("获取视频格式失败", e);
            return "unknown";
        }
    }

    /**
     * 获取音频时长（秒）
     */
    public static double getAudioDuration(String audioPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                audioPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            process.waitFor();

            if (line != null && !line.isEmpty()) {
                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            logger.error("获取音频时长失败", e);
        }
        return 0;
    }

    /**
     * 将音频文件切片为多个片段
     * @param audioPath 原始音频文件路径
     * @param segmentDuration 每个片段的时长（秒）
     * @return 切片后的音频文件路径列表
     */
    public static List<String> splitAudioFile(String audioPath, int segmentDuration) {
        logger.info("开始切片音频文件: {}, 每片{}秒", audioPath, segmentDuration);
        List<String> segments = new ArrayList<>();

        try {
            // 获取音频总时长
            double totalDuration = getAudioDuration(audioPath);
            logger.info("音频总时长: {} 秒", totalDuration);

            if (totalDuration <= segmentDuration) {
                // 如果音频时长小于限制，直接返回原文件
                logger.info("音频时长小于{}秒，无需切片", segmentDuration);
                segments.add(audioPath);
                return segments;
            }

            // 计算需要切片的数量
            int segmentCount = (int) Math.ceil(totalDuration / segmentDuration);
            logger.info("需要切分为 {} 个片段", segmentCount);

            // 生成切片文件
            File audioFile = new File(audioPath);
            String baseName = audioFile.getName().replace(".wav", "");
            String parentDir = audioFile.getParent();

            for (int i = 0; i < segmentCount; i++) {
                double startTime = i * segmentDuration;
                String segmentPath = parentDir + File.separator + baseName + "_part" + (i + 1) + ".wav";

                // 使用ffmpeg切片
                ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", audioPath,
                    "-ss", String.valueOf(startTime),
                    "-t", String.valueOf(segmentDuration),
                    "-acodec", "copy",
                    segmentPath
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    File segmentFile = new File(segmentPath);
                    if (segmentFile.exists() && segmentFile.length() > 0) {
                        segments.add(segmentPath);
                        logger.info("切片 {} 创建成功: {} ({}-{}秒)",
                            i + 1, segmentPath, startTime, startTime + segmentDuration);
                    }
                } else {
                    logger.error("切片 {} 创建失败", i + 1);
                }
            }

            logger.info("音频切片完成，共 {} 个片段", segments.size());

        } catch (Exception e) {
            logger.error("音频切片失败", e);
        }

        return segments;
    }
}