import { create } from 'zustand';
import type { Video, Subtitle, TaskRecord } from '../types';

interface AppStore {
  // 状态
  videos: Video[];
  subtitles: Subtitle[];
  currentVideo: Video | null;
  currentSubtitles: Subtitle[];
  tasks: TaskRecord[];

  // 加载状态
  loading: boolean;
  uploading: boolean;
  generating: boolean;

  // 错误信息
  error: string | null;

  // Actions
  setVideos: (videos: Video[]) => void;
  setVideo: (video: Video) => void;
  addVideo: (video: Video) => void;
  removeVideo: (id: number) => void;

  setSubtitles: (subtitles: Subtitle[]) => void;
  setSubtitlesByVideoId: (videoId: number, subtitles: Subtitle[]) => void;
  addSubtitle: (subtitle: Subtitle) => void;

  setTasks: (tasks: TaskRecord[]) => void;
  updateTask: (taskId: string, task: Partial<TaskRecord>) => void;

  setLoading: (loading: boolean) => void;
  setUploading: (uploading: boolean) => void;
  setGenerating: (generating: boolean) => void;

  setError: (error: string | null) => void;

  // 重置
  reset: () => void;
}

export const useAppStore = create<AppStore>((set, get) => ({
  // 初始状态
  videos: [],
  subtitles: [],
  currentVideo: null,
  currentSubtitles: [],
  tasks: [],

  loading: false,
  uploading: false,
  generating: false,

  error: null,

  // Actions
  setVideos: (videos) => set({ videos }),
  setVideo: (video) => set({ currentVideo: video }),
  addVideo: (video) => set((state) => ({ videos: [...state.videos, video] })),
  removeVideo: (id) => set((state) => ({
    videos: state.videos.filter(v => v.id !== id),
    currentVideo: state.currentVideo?.id === id ? null : state.currentVideo,
  })),

  setSubtitles: (subtitles) => set({ subtitles }),
  setSubtitlesByVideoId: (videoId, subtitles) => set((state) => ({
    subtitles: [...state.subtitles.filter(s => s.videoId !== videoId), ...subtitles],
    currentSubtitles: subtitles,
  })),
  addSubtitle: (subtitle) => set((state) => ({
    subtitles: [...state.subtitles, subtitle],
  })),

  setTasks: (tasks) => set({ tasks }),
  updateTask: (taskId, task) => set((state) => ({
    tasks: state.tasks.map(t => t.taskId === taskId ? { ...t, ...task } : t),
  })),

  setLoading: (loading) => set({ loading }),
  setUploading: (uploading) => set({ uploading }),
  setGenerating: (generating) => set({ generating }),

  setError: (error) => set({ error }),

  // 重置
  reset: () => set({
    videos: [],
    subtitles: [],
    currentVideo: null,
    currentSubtitles: [],
    tasks: [],
    loading: false,
    uploading: false,
    generating: false,
    error: null,
  }),
}));