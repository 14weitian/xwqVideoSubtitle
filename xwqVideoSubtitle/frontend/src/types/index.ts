// 用户类型定义
export interface User {
  id: number;
  username: string;
  email: string;
  avatar?: string;  // 用户头像路径
  nickname: string;
  status: number;
  createdAt: string;
}

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
}

// 注册请求
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  nickname?: string;
}

// 登录响应
export interface LoginResponse {
  token: string;
  user: User;
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// 更新用户信息请求
export interface UpdateProfileRequest {
  nickname?: string;
  email?: string;
}

// 视频类型定义
export interface Video {
  id: number;
  userId?: number;
  title: string;
  fileName: string;
  filePath: string;
  fileSize: number;
  duration: string;
  format: string;
  status: number;
  progress: number;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

// 字幕片段类型定义
export interface SubtitleSegment {
  index: number;
  startTime: number;
  endTime: number;
  text: string;
  confidence?: number;
  speaker?: number;
  duration?: number;
  alternatives?: string[];
}

// 字幕类型定义
export interface Subtitle {
  id: number;
  videoId: number;
  language: string;
  content: string;
  format: string;
  status: number;
  errorMessage?: string;
  duration?: number;
  segmentCount?: number;
  filePath?: string;
  createdAt: string;
  updatedAt: string;
}

// 任务记录类型定义
export interface TaskRecord {
  id: number;
  taskId: string;
  taskType: string;
  videoId?: number;
  status: number;
  progress: number;
  message: string;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

// API响应类型定义
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 视频上传DTO
export interface VideoUploadDTO {
  title?: string;
  language: string;
}

// 字幕生成DTO
export interface SubtitleGenerateDTO {
  videoId: number;
  language: string;
  format?: string;
}

// 支持的语言列表
export interface LanguageOption {
  code: string;
  name: string;
  nativeName: string;
}