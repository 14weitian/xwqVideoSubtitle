# 智谱AI语音识别集成说明

## 概述

本项目已成功集成智谱AI的GLM-ASR语音识别服务,支持将音频文件转换为字幕文本。

## 功能特点

- ✅ **中文优化**: 针对普通话、粤语等中文场景优化
- ✅ **高准确率**: 语音识别准确率达97.8%
- ✅ **多场景支持**: 适用于会议录音、视频字幕、客服语音等场景
- ✅ **易于切换**: 通过配置文件即可在不同STT服务商间切换

## 配置说明

### 1. 获取API密钥

1. 访问 [智谱AI开放平台](https://open.bigmodel.cn/)
2. 注册并登录账号
3. 进入控制台 → API Keys 页面
4. 点击"添加新的API Key"创建密钥

### 2. 配置API密钥

#### 方式一: 环境变量(推荐)

```bash
# Linux/macOS
export ZHIPU_API_KEY="your_api_key_here"

# Windows CMD
set ZHIPU_API_KEY=your_api_key_here

# Windows PowerShell
$env:ZHIPU_API_KEY="your_api_key_here"
```

#### 方式二: 直接修改配置文件

编辑 `application.yml`:

```yaml
app:
  stt:
    provider: zhipu  # 切换到智谱AI
    zhipu:
      api-key: your_api_key_here  # 直接填写API密钥
      endpoint: https://open.bigmodel.cn/api/paas/v4/audio/transcriptions
      model: glm-asr
      language: zh
      timeout: 60000
```

**⚠️ 注意**: 直接在配置文件中填写API密钥存在安全风险,不建议在生产环境使用。

### 3. 配置参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `api-key` | 智谱AI的API密钥 | 必填 |
| `endpoint` | API端点地址 | `https://open.bigmodel.cn/api/paas/v4/audio/transcriptions` |
| `model` | 使用的模型名称 | `glm-asr` |
| `language` | 识别语言 | `zh`(中文) |
| `timeout` | 请求超时时间(毫秒) | `60000`(60秒) |

## 支持的语言

智谱AI GLM-ASR支持多种语言:

- `zh`: 中文普通话
- `zh-yue`: 粤语
- `en`: 英语
- 更多语言请参考[智谱AI官方文档](https://docs.bigmodel.cn)

## 使用方法

### 1. 启动应用

```bash
cd xwqVideoSubtitle/backend
mvn spring-boot:run
```

### 2. 上传视频生成字幕

应用会自动使用智谱AI进行语音识别:

1. 上传视频文件
2. 系统提取音频
3. 调用智谱AI API进行转写
4. 生成字幕文件

### 3. 切换STT服务商

修改 `application.yml` 中的 `app.stt.provider`:

```yaml
app:
  stt:
    provider: zhipu  # 可选: azure, whisper, google, zhipu
```

## 价格说明

- 智谱AI GLM-ASR: 约 **0.06元/分钟**
- 具体价格请以[智谱AI官网](https://open.bigmodel.cn/)为准

## 技术实现

### 核心类

- **ZhipuSttConfig**: 智谱AI配置类 (`config/ZhipuSttConfig.java`)
- **ZhipuSttServiceImpl**: STT服务实现 (`service/impl/ZhipuSttServiceImpl.java`)

### 工作流程

1. 接收音频文件路径
2. 构建HTTP multipart请求
3. 调用智谱AI API
4. 解析JSON响应
5. 转换为字幕片段对象
6. 返回字幕列表

## 故障排查

### 常见错误

#### 1. API密钥无效

```
错误信息: 401 Unauthorized
解决方案: 检查API密钥是否正确配置
```

#### 2. 请求超时

```
错误信息: Request timeout
解决方案: 增加 timeout 配置值
```

#### 3. 文件格式不支持

```
错误信息: Unsupported audio format
解决方案: 确保音频文件为常见格式(WAV, MP3, M4A等)
```

### 查看日志

```bash
# 查看应用日志
tail -f logs/application.log

# 或在控制台查看实时日志
```

## 相关链接

- [智谱AI开放平台](https://open.bigmodel.cn/)
- [智谱AI API文档](https://docs.bigmodel.cn/cn/api/introduction)
- [GLM-ASR使用指南](https://docs.bigmodel.cn/cn/guide/models/sound-and-video/glm-asr)
- [价格说明](https://open.bigmodel.cn/pricing)

## 许可证

本项目遵循 MIT 许可证。

## 更新日志

### v1.0.0 (2026-02-27)
- ✨ 新增智谱AI GLM-ASR集成
- ✨ 支持音频文件转写
- ✨ 支持多语言识别
- ⚠️ 实时流式转写功能待实现
