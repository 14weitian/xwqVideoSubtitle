import axios from 'axios';
import type { Video, Subtitle, TaskRecord, ApiResponse, LanguageOption } from '../types';

const API_BASE_URL = 'http://localhost:8080';

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
    // 可以在这里添加token等认证信息
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
    console.error('API请求错误:', error);
    throw error;
  }
);

// 视频相关API
export const videoApi = {
  // 上传视频
  upload: (file: File, title?: string, language = 'zh-CN') => {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    formData.append('language', language);

    return api.post<ApiResponse<Video>>('/videos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 获取视频列表
  getList: () => api.get<ApiResponse<Video[]>>('/videos'),

  // 获取视频详情
  getById: (id: number) => api.get<ApiResponse<Video>>(`/videos/${id}`),

  // 删除视频
  delete: (id: number) => api.delete<ApiResponse<void>>(`/videos/${id}`),

  // 验证视频格式
  validateFormat: (filename: string) => api.post<ApiResponse<boolean>>('/videos/validate', null, {
    params: { filename },
  }),
};

// 字幕相关API
export const subtitleApi = {
  // 生成字幕
  generate: (videoId: number, language: string, format = 'json') => {
    return api.post<ApiResponse<string>>('/subtitles/generate', {
      videoId,
      language,
      format,
    });
  },

  // 获取任务状态
  getTaskStatus: (taskId: string) => api.get<ApiResponse<TaskRecord>>(`/subtitles/task/${taskId}`),

  // 获取视频的所有字幕
  getByVideoId: (videoId: number) => api.get<ApiResponse<Subtitle[]>>(`/subtitles/video/${videoId}`),

  // 获取字幕详情
  getById: (id: number) => api.get<ApiResponse<Subtitle>>(`/subtitles/${id}`),

  // 导出字幕文件
  exportFile: (id: number, format = 'srt') => {
    return api.get(`/subtitles/${id}/export`, {
      responseType: 'blob',
      params: { format },
    });
  },

  // 删除字幕
  delete: (id: number) => api.delete<ApiResponse<void>>(`/subtitles/${id}`),
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