# xwqVideoSubtitle - 视频字幕生成系统

一个基于Spring Boot + React的全栈应用，能够将视频文件转换为对应的中文字幕。

## 功能特性

- 🎥 **视频上传**: 支持多种视频格式（MP4, AVI, MKV, MOV, WMV, FLV）
- 🔊 **音频提取**: 使用FFmpeg从视频中提取音频
- 🎯 **字幕生成**: 支持多种语音识别服务（Azure Speech、OpenAI Whisper、Google Cloud Speech）
- 🌐 **多语言支持**: 支持中文简体/繁体、英语、日语、韩语等多种语言
- 📄 **字幕格式**: 支持SRT、VTT等主流字幕格式
- ⚡ **异步处理**: 长时间任务异步处理，实时查看进度
- 💾 **文件管理**: 完整的视频和字幕文件管理

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.5
- **视频处理**: FFmpeg (javacv)
- **语音识别**: Azure Speech / OpenAI Whisper / Google Cloud Speech
- **异步任务**: Spring Async
- **构建工具**: Maven

### 前端
- **框架**: React 18 + TypeScript
- **构建工具**: Vite
- **状态管理**: Zustand
- **UI组件**: 自定义组件 + Tailwind CSS
- **HTTP客户端**: Axios

## 快速开始

### 环境要求

- JDK 21
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd xwqVideoSubtitle
   ```

2. **启动数据库**
   ```bash
   docker-compose up -d mysql
   ```

3. **配置环境变量**
   ```bash
   # 创建环境变量文件（可选）
   echo "export AZURE_SPEECH_KEY=your_azure_key" >> ~/.bashrc
   echo "export AZURE_SPEECH_REGION=your_azure_region" >> ~/.bashrc
   echo "export OPENAI_API_KEY=your_openai_key" >> ~/.bashrc
   ```

4. **启动服务**
   ```bash
   ./start.sh
   ```

5. **访问应用**
   - 前端: http://localhost:5173
   - 后端API: http://localhost:8080
   - 健康检查: http://localhost:8080/api/actuator/health

## 项目结构

```
xwqVideoSubtitle/
├── backend/                     # Spring Boot后端
│   ├── src/main/java/com/subtitle/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── entity/            # 实体类
│   │   ├── mapper/            # MyBatis-Plus映射
│   │   ├── service/           # 业务逻辑
│   │   └── utils/             # 工具类
│   └── pom.xml               # Maven配置
├── frontend/                   # React前端
│   ├── src/
│   │   ├── components/       # React组件
│   │   ├── services/         # API服务
│   │   ├── store/            # 状态管理
│   │   └── types/            # TypeScript类型
│   └── package.json          # 前端依赖
└── docker-compose.yml        # 数据库服务
└── README.md                # 项目说明

## API文档

### 视频管理

- `POST /api/videos/upload` - 上传视频
- `GET /api/videos` - 获取视频列表
- `GET /api/videos/{id}` - 获取视频详情
- `DELETE /api/videos/{id}` - 删除视频

### 字幕管理

- `POST /api/subtitles/generate` - 生成字幕
- `GET /api/subtitles/task/{taskId}` - 查询任务状态
- `GET /api/subtitles/video/{videoId}` - 获取视频字幕列表
- `GET /api/subtitles/{id}` - 获取字幕详情
- `GET /api/subtitles/{id}/export` - 导出字幕文件
- `DELETE /api/subtitles/{id}` - 删除字幕

## 配置说明

### 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/subtitle_db
    username: subtitle
    password: subtitle123

# 文件上传配置
servlet:
  multipart:
    max-file-size: 500MB

# STT服务配置
app:
  stt:
    provider: azure  # 可选: azure, whisper, google
```

### 环境变量

```bash
# Azure Speech
AZURE_SPEECH_KEY=your_key
AZURE_SPEECH_REGION=your_region
AZURE_SPEECH_ENDPOINT=your_endpoint

# OpenAI Whisper
OPENAI_API_KEY=your_key

# Google Cloud Speech
GOOGLE_APPLICATION_CREDENTIALS=path/to/credentials.json
```

## 开发指南

### 添加新的STT服务

1. 实现 `SttService` 接口
2. 创建相应的 `@Service` 实现类
3. 添加条件注解 `@ConditionalOnProperty`
4. 在配置中添加相应配置项

### 自定义字幕格式

扩展 `SubtitleFormatConverter` 类：

```java
public static String convertToCustomFormat(List<SubtitleSegment> segments) {
    // 实现自定义格式转换
}
```

## 常见问题

### Q: 视频上传失败？
A: 检查文件格式和大小限制，确保支持的视频格式和500MB大小限制。

### Q: 字幕生成失败？
A: 检查STT服务配置，确保API密钥正确且有效。

### Q: 无法连接数据库？
A: 确保MySQL服务启动，检查连接参数是否正确。

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！