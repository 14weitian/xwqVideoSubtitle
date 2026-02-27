# 智谱AI音频切片功能说明

## 问题背景

智谱AI API对音频文件有时长限制：
- **限制时长**：30秒
- **错误信息**：`transcriptions文件时长限制为0-30秒`
- **影响**：无法直接处理超过30秒的视频

## 解决方案

实现**音频自动切片**功能：
1. 检测音频时长
2. 如果超过25秒（留5秒余量），自动切分成多个片段
3. 依次识别每个片段
4. 合并识别结果，调整时间戳

## 实现细节

### 1. 音频时长检测

**文件**：`AudioExtractor.java`

```java
public static double getAudioDuration(String audioPath) {
    // 使用ffprobe获取音频时长
    ProcessBuilder pb = new ProcessBuilder(
        "ffprobe",
        "-v", "error",
        "-show_entries", "format=duration",
        "-of", "default=noprint_wrappers=1:nokey=1",
        audioPath
    );
    // 返回时长（秒）
}
```

### 2. 音频切片

```java
public static List<String> splitAudioFile(String audioPath, int segmentDuration) {
    // 获取总时长
    double totalDuration = getAudioDuration(audioPath);

    // 计算需要切分的数量
    int segmentCount = (int) Math.ceil(totalDuration / segmentDuration);

    // 使用ffmpeg切分
    for (int i = 0; i < segmentCount; i++) {
        double startTime = i * segmentDuration;
        // ffmpeg -i input.wav -ss {startTime} -t {duration} -acodec copy output_part{i}.wav
    }

    return segments;
}
```

### 3. 分段识别与合并

**文件**：`ZhipuSttServiceImpl.java`

```java
public List<SubtitleSegment> transcribeFile(String audioPath, String language) {
    // 1. 检查音频时长
    double audioDuration = AudioExtractor.getAudioDuration(audioPath);

    // 2. 切片（如果需要）
    List<String> audioSegments;
    if (audioDuration > 25) {
        audioSegments = AudioExtractor.splitAudioFile(audioPath, 25);
    } else {
        audioSegments = [audioPath];
    }

    // 3. 依次识别
    List<SubtitleSegment> allSegments = new ArrayList<>();
    double timeOffset = 0;

    for (String segmentPath : audioSegments) {
        List<SubtitleSegment> segmentResult = transcribeSingleFile(segmentPath, language);

        // 4. 调整时间戳
        for (SubtitleSegment segment : segmentResult) {
            segment.setStartTime(segment.getStartTime() + timeOffset);
            segment.setEndTime(segment.getEndTime() + timeOffset);
            allSegments.add(segment);
        }

        // 5. 更新时间偏移
        timeOffset += getAudioDuration(segmentPath);
    }

    return allSegments;
}
```

## 工作流程

```
原始音频（60秒）
    ↓
检测时长：60秒 > 25秒
    ↓
切分为3个片段：
  - part1.wav (0-25秒)
  - part2.wav (25-50秒)
  - part3.wav (50-60秒)
    ↓
依次识别：
  - part1 → 字幕1 (时间: 0-25秒)
  - part2 → 字幕2 (时间: 0-25秒) → 调整为 (25-50秒)
  - part3 → 字幕3 (时间: 0-10秒) → 调整为 (50-60秒)
    ↓
合并结果：
  - 字幕1 + 字幕2 + 字幕3
  - 时间戳已调整
    ↓
返回完整字幕列表
```

## 示例

### 示例1：短视频（20秒）

```
音频时长: 20秒
判断: 20 <= 25
操作: 无需切片
结果: 直接识别整个文件
```

### 示例2：中等视频（45秒）

```
音频时长: 45秒
判断: 45 > 25
切片:
  - part1.wav (0-25秒)
  - part2.wav (25-45秒)

识别:
  - part1 → 5个字幕片段 (0-25秒)
  - part2 → 3个字幕片段 (0-20秒)

时间调整:
  - part2的字幕时间戳 +25秒

合并:
  - 共8个字幕片段 (0-45秒)
```

### 示例3：长视频（2分钟）

```
音频时长: 120秒
切片数: ceil(120/25) = 5个片段
  - part1 (0-25秒)
  - part2 (25-50秒)
  - part3 (50-75秒)
  - part4 (75-100秒)
  - part5 (100-120秒)

处理时间: 约 5 × 2秒 = 10秒
```

## 性能影响

### 时间开销

| 视频时长 | 切片数 | 识别时间（估算） |
|---------|-------|-----------------|
| 20秒 | 1 | 2-3秒 |
| 1分钟 | 3 | 6-9秒 |
| 5分钟 | 12 | 24-36秒 |
| 10分钟 | 24 | 48-72秒 |

**注意**：
- 切片本身很快（ffmpeg很快）
- 主要时间在API调用
- 串行处理，保证顺序正确

### 存储开销

临时文件存储在 `./uploads/audio/` 目录：
- 原始音频：`4.wav`
- 切片文件：`4_part1.wav`, `4_part2.wav`, ...

**清理建议**：识别完成后可删除临时切片文件

## 配置参数

### 切片时长

```java
final int MAX_DURATION = 25; // 秒
```

**为什么是25秒而不是30秒？**
- 留5秒余量
- 避免边界情况
- 确保不超过API限制

### 调整建议

根据实际API响应速度，可以调整：
- **快速API**：可增加到28秒
- **慢速API**：可减少到20秒

## 日志示例

### 短视频（无需切片）

```
INFO: 开始使用智谱AI GLM-ASR转写音频文件: ./uploads/audio/1.wav
INFO: 请求识别语言: auto
INFO: 音频时长: 15.5 秒
INFO: 音频时长在限制内，无需切片
INFO: 正在识别第 1/1 个片段
INFO: 智谱AI转写成功
INFO: 解析到 8 个字幕片段
INFO: 所有片段识别完成，共 8 个字幕片段
```

### 长视频（需要切片）

```
INFO: 开始使用智谱AI GLM-ASR转写音频文件: ./uploads/audio/2.wav
INFO: 请求识别语言: auto
INFO: 音频时长: 65.3 秒
INFO: 音频时长超过25秒，开始切片...
INFO: 开始切片音频文件: ./uploads/audio/2.wav, 每片25秒
INFO: 音频总时长: 65.3 秒
INFO: 需要切分为 3 个片段
INFO: 切片 1 创建成功: ./uploads/audio/2_part1.wav (0.0-25.0秒)
INFO: 切片 2 创建成功: ./uploads/audio/2_part2.wav (25.0-50.0秒)
INFO: 切片 3 创建成功: ./uploads/audio/2_part3.wav (50.0-75.0秒)
INFO: 音频切片完成，共 3 个片段
INFO: 音频已切分为 3 个片段
INFO: 正在识别第 1/3 个片段: ./uploads/audio/2_part1.wav
INFO: 智谱AI转写成功
INFO: 解析到 12 个字幕片段
INFO: 第 1 个片段识别完成，获得 12 个字幕片段
INFO: 正在识别第 2/3 个片段: ./uploads/audio/2_part2.wav
INFO: 智谱AI转写成功
INFO: 解析到 10 个字幕片段
INFO: 第 2 个片段识别完成，获得 10 个字幕片段
INFO: 正在识别第 3/3 个片段: ./uploads/audio/2_part3.wav
INFO: 智谱AI转写成功
INFO: 解析到 5 个字幕片段
INFO: 第 3 个片段识别完成，获得 5 个字幕片段
INFO: 所有片段识别完成，共 27 个字幕片段
```

## 错误处理

### 切片失败

```java
if (exitCode != 0) {
    logger.error("切片 {} 创建失败", i + 1);
    // 继续处理其他切片
}
```

### 部分切片识别失败

```java
try {
    List<SubtitleSegment> segmentResult = transcribeSingleFile(segmentPath, language);
    // 处理结果
} catch (Exception e) {
    logger.error("转写单个音频文件失败: " + segmentPath, e);
    // 返回空列表，继续下一个
}
```

## 依赖要求

### FFmpeg & FFprobe

确保系统已安装：
```bash
# macOS
brew install ffmpeg

# 验证
ffmpeg -version
ffprobe -version
```

## 优化建议

### 1. 并行处理

当前是串行处理，可以改为并行：
```java
CompletableFuture<List<SubtitleSegment>>[] futures = new CompletableFuture[audioSegments.size()];
// 并行识别
// 合并结果
```

**权衡**：
- ✅ 速度更快
- ❌ 需要处理并发
- ❌ 可能触发API限流

### 2. 缓存机制

对相同音频缓存识别结果：
```java
String cacheKey = DigestUtils.md5Hex(Files.readAllBytes(Paths.get(audioPath)));
if (cache.has(cacheKey)) {
    return cache.get(cacheKey);
}
```

### 3. 断点续传

保存已识别的片段，失败后可从断点继续：
```java
// 保存进度
// 重启后恢复
```

## 测试场景

### 测试1：短视频（15秒）

**预期**：无需切片，直接识别

### 测试2：中等视频（45秒）

**预期**：切分为2个片段，时间戳正确

### 测试3：长视频（2分钟）

**预期**：切分为5个片段，所有片段正确合并

### 测试4：边界值（30秒）

**预期**：切分为2个片段（25秒 + 5秒）

## 相关文档

- [智谱AI集成说明.md](./智谱AI集成说明.md)
- [自动语言检测功能.md](./自动语言检测功能.md)

---

**更新时间**：2026-02-27
**版本**：v1.3.0
