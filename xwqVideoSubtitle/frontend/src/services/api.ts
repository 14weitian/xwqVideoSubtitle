import axios from 'axios';
import type { ApiResponse, LanguageOption, LoginResponse, Subtitle, TaskRecord, User, Video } from '../types';

const API_BASE_URL = 'http://localhost:8081/api';

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token 并添加到请求头
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.code === 200) {
      return data.data;
    } else {
      throw new Error(data.message || '请求失败');
    }
  },
  (error) => {
    // 处理 401 未授权错误
    if (error.response?.status === 401) {
      // 清除 token
      localStorage.removeItem('token');
      // 跳转到登录页面
      window.location.href = '/login';
    }
    //console.error('API请求错误:', error);
    throw error;
  }
);

// 认证相关API
export const authApi = {
  // 用户登录
  login: async (username: string, password: string): Promise<LoginResponse> => {
    const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', {
      username,
      password,
    });
    return response as any;
  },

  // 用户注册
  register: async (username: string, email: string, password: string, nickname?: string): Promise<LoginResponse> => {
    const response = await api.post<ApiResponse<LoginResponse>>('/auth/register', {
      username,
      email,
      password,
      nickname,
    });
    return response as any;
  },

  // 检查用户名是否已存在
  checkUsername: async (username: string): Promise<boolean> => {
    const response = await api.get<ApiResponse<boolean>>('/auth/check-username', {
      params: { username },
    });
    return response as any;
  },

  // 检查邮箱是否已存在
  checkEmail: async (email: string): Promise<boolean> => {
    const response = await api.get<ApiResponse<boolean>>('/auth/check-email', {
      params: { email },
    });
    return response as any;
  },

  // 获取当前用户信息
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<ApiResponse<User>>('/auth/me');
    return response as any;
  },

  // 修改密码
  changePassword: async (oldPassword: string, newPassword: string, confirmPassword: string): Promise<void> => {
    await api.post<ApiResponse<void>>('/auth/change-password', {
      oldPassword,
      newPassword,
      confirmPassword,
    });
  },

  // 更新用户信息
  updateProfile: async (nickname?: string, email?: string): Promise<User> => {
    const response = await api.put<ApiResponse<User>>('/auth/profile', {
      nickname,
      email,
    });
    return response as any;
  },

  // 上传头像
  uploadAvatar: async (file: File): Promise<User> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<ApiResponse<User>>('/auth/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response as any;
  },

  // 发送重置密码验证码
  sendResetCode: async (email: string): Promise<void> => {
    await api.post<ApiResponse<void>>('/auth/forgot-password/send-code', { email });
  },

  // 重置密码
  resetPassword: async (email: string, code: string, newPassword: string, confirmPassword: string): Promise<void> => {
    await api.post<ApiResponse<void>>('/auth/forgot-password/reset', {
      email,
      code,
      newPassword,
      confirmPassword,
    });
  },
};

// 视频相关API
export const videoApi = {
  // 上传视频
  upload: async (file: File, title?: string, language = 'zh-CN'): Promise<Video> => {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    formData.append('language', language);

    const response = await api.post<ApiResponse<Video>>('/videos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response as any;
  },

  // 获取视频列表
  getList: async (): Promise<Video[]> => {
    const response = await api.get<ApiResponse<Video[]>>('/videos');
    return response as any;
  },

  // 获取视频详情
  getById: async (id: number): Promise<Video> => {
    const response = await api.get<ApiResponse<Video>>(`/videos/${id}`);
    return response as any;
  },

  // 删除视频
  delete: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`/videos/${id}`);
  },

  // 验证视频格式
  validateFormat: async (filename: string): Promise<boolean> => {
    const response = await api.post<ApiResponse<boolean>>('/videos/validate', null, {
      params: { filename },
    });
    return response as any;
  },
};

// 字幕相关API
export const subtitleApi = {
  // 生成字幕
  generate: async (videoId: number, language: string, format = 'json'): Promise<string> => {
    const response = await api.post<ApiResponse<string>>('/subtitles/generate', {
      videoId,
      language,
      format,
    });
    return response as any;
  },

  // 获取任务状态
  getTaskStatus: async (taskId: string): Promise<TaskRecord> => {
    const response = await api.get<ApiResponse<TaskRecord>>(`/subtitles/task/${taskId}`);
    return response as any;
  },

  // 获取视频的所有字幕
  getByVideoId: async (videoId: number): Promise<Subtitle[]> => {
    const response = await api.get<ApiResponse<Subtitle[]>>(`/subtitles/video/${videoId}`);
    return response as any;
  },

  // 获取字幕详情
  getById: async (id: number): Promise<Subtitle> => {
    const response = await api.get<ApiResponse<Subtitle>>(`/subtitles/${id}`);
    return response as any;
  },

  // 导出字幕文件
  exportFile: async (id: number, format = 'srt'): Promise<Blob> => {
    const response = await api.get(`/subtitles/${id}/export`, {
      responseType: 'blob',
      params: { format },
    });
    return response as any;
  },

  // 删除字幕
  delete: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`/subtitles/${id}`);
  },
};

// 系统相关API
export const systemApi = {
  // 获取支持的语言列表
  getLanguages: (): LanguageOption[] => [
    { code: 'zh-CN', name: '中文（简体）', nativeName: '简体中文' },
    { code: 'zh-TW', name: '中文（繁体）', nativeName: '繁體中文' },
    { code: 'en-US', name: '英语（美国）', nativeName: 'English (US)' },
    { code: 'ja-JP', name: '日语', nativeName: '日本語' },
    { code: 'ko-KR', name: '韩语', nativeName: '한국어' },
  ],
};

export default api;