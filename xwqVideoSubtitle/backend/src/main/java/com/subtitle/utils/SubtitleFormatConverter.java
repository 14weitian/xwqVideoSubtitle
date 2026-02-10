package com.subtitle.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subtitle.entity.SubtitleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SubtitleFormatConverter {

    private static final Logger logger = LoggerFactory.getLogger(SubtitleFormatConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DecimalFormat timeFormat = new DecimalFormat("00");

    /**
     * JSON转换为SRT格式
     */
    public static String convertToSrt(List<SubtitleSegment> segments) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            SubtitleSegment segment = segments.get(i);

            // 序号
            sb.append(i + 1).append("\n");

            // 时间格式：00:00:00,000 --> 00:00:01,000
            String startTime = formatTime(segment.getStartTime());
            String endTime = formatTime(segment.getEndTime());
            sb.append(startTime).append(" --> ").append(endTime).append("\n");

            // 字幕内容
            sb.append(segment.getText()).append("\n\n");
        }

        return sb.toString();
    }

    /**
     * JSON转换为VTT格式
     */
    public static String convertToVtt(List<SubtitleSegment> segments) {
        StringBuilder sb = new StringBuilder();

        // VTT头部
        sb.append("WEBVTT\n\n");

        for (int i = 0; i < segments.size(); i++) {
            SubtitleSegment segment = segments.get(i);

            // 序号（可选）
            sb.append("").append(i + 1).append("\n");

            // 时间格式：00:00:00.000 --> 00:00:01.000
            String startTime = formatTimeVtt(segment.getStartTime());
            String endTime = formatTimeVtt(segment.getEndTime());
            sb.append(startTime).append(" --> ").append(endTime).append("\n");

            // 字幕内容
            sb.append(segment.getText()).append("\n\n");
        }

        return sb.toString();
    }

    /**
     * 保存字幕到文件
     */
    public static void saveSubtitleFile(String content, String filePath) throws IOException {
        File file = new File(filePath);

        // 确保父目录存在
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(content);
        }

        logger.info("字幕文件已保存: {}", filePath);
    }

    /**
     * 从SRT文件加载字幕
     */
    public static List<SubtitleSegment> loadFromSrt(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("SRT文件不存在: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<SubtitleSegment> segments = reader.lines()
                    .collect(Collectors.groupingBy(line -> line.trim().isEmpty()))
                    .entrySet().stream()
                    .filter(entry -> !entry.getKey())
                    .flatMap(entry -> parseSrtBlock(entry.getValue().toArray(new String[0])))
                    .collect(Collectors.toList());

            logger.info("成功加载SRT字幕: {} 个片段", segments.size());
            return segments;
        }
    }

    /**
     * 解析SRT块
     */
    private static java.util.stream.Stream<SubtitleSegment> parseSrtBlock(String[] block) {
        if (block.length < 3) {
            return java.util.stream.Stream.empty();
        }

        try {
            int index = Integer.parseInt(block[0].trim());

            // 解析时间行
            String[] timeParts = block[1].trim().split("\\s*-->\\s*");
            String startTime = timeParts[0].trim();
            String endTime = timeParts[1].trim();

            // 解析字幕文本
            String text = String.join("\n", Arrays.copyOfRange(block, 2, block.length)).trim();

            SubtitleSegment segment = new SubtitleSegment();
            segment.setIndex(index);
            segment.setStartTime(parseTimeToSeconds(startTime));
            segment.setEndTime(parseTimeToSeconds(endTime));
            segment.setText(text);
            segment.calculateDuration();

            return java.util.stream.Stream.of(segment);
        } catch (Exception e) {
            logger.warn("解析SRT块失败: {}", e.getMessage());
            return java.util.stream.Stream.empty();
        }
    }

    /**
     * 将时间字符串转换为秒数
     */
    private static double parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split("[,.:]");
        double seconds = 0;

        try {
            // HH:MM:SS,mmm 或 HH:MM:SS.mmm
            if (parts.length >= 4) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int secs = Integer.parseInt(parts[2]);
                int millis = Integer.parseInt(parts[3]);

                seconds = hours * 3600 + minutes * 60 + secs + millis / 1000.0;
            }
        } catch (Exception e) {
            logger.warn("解析时间失败: {}", timeStr);
        }

        return seconds;
    }

    /**
     * 格式化时间为SRT格式
     */
    private static String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds % 1) * 1000);

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }

    /**
     * 格式化时间为VTT格式
     */
    private static String formatTimeVtt(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds % 1) * 1000);

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, secs, millis);
    }
}