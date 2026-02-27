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

public class AudioExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AudioExtractor.class);

    /**
     * 从视频中提取音频
     * @param videoPath 视频文件路径
     * @param audioPath 输出的音频文件路径
     * @return 是否提取成功
     */
    public static boolean extractAudio(String videoPath, String audioPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {

            // 设置音频格式
            grabber.setOption("acodec", "pcm_s16le");
            grabber.setAudioChannels(1);
            grabber.setSampleRate(16000);

            grabber.start();

            // 检查是否有音频流
            if (grabber.getAudioStream() == -1) {
                logger.error("视频中未找到音频流");
                return false;
            }

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(audioPath, 1, 16000)) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_PCM_S16LE);
                recorder.setFormat("wav");
                recorder.start();

                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    if (frame.samples != null) {
                        recorder.record(frame);
                    }
                }
                recorder.stop();
            }

            grabber.stop();
            logger.info("音频提取成功: {}", audioPath);
            return true;

        } catch (IOException e) {
            logger.error("音频提取失败", e);
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
}