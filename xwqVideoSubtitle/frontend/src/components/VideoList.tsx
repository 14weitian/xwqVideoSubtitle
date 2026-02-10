import React from 'react';
import { videoApi } from '../services/api';
import { useAppStore } from '../store';
import type { Video } from '../types';

interface VideoListProps {
  videos: Video[];
  onVideoSelect: (video: Video) => void;
  onDeleteVideo?: (id: number) => void;
}

const VideoList: React.FC<VideoListProps> = ({ videos, onVideoSelect, onDeleteVideo }) => {
  const { setError } = useAppStore();

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除这个视频吗？')) return;

    try {
      await videoApi.delete(id);
      onDeleteVideo?.(id);
    } catch (error: any) {
      setError(error.message || '删除失败');
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getStatusText = (status: number): string => {
    switch (status) {
      case 0: return '上传中';
      case 1: return '已完成';
      case 2: return '处理中';
      case 3: return '失败';
      default: return '未知';
    }
  };

  const getStatusColor = (status: number): string => {
    switch (status) {
      case 0: return 'bg-yellow-100 text-yellow-800';
      case 1: return 'bg-green-100 text-green-800';
      case 2: return 'bg-blue-100 text-blue-800';
      case 3: return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  if (videos.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-8 text-center">
        <div className="text-6xl mb-4">📹</div>
        <p className="text-gray-500 text-lg">暂无视频</p>
        <p className="text-gray-400 mt-2">请上传视频文件开始处理</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4">视频列表</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {videos.map((video) => (
          <div
            key={video.id}
            className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
            onClick={() => onVideoSelect(video)}
          >
            {/* 视频预览（简单占位） */}
            <div className="aspect-video bg-gray-100 rounded mb-3 flex items-center justify-center">
              <div className="text-4xl">🎬</div>
            </div>

            <div className="space-y-2">
              <div>
                <h3 className="font-medium text-gray-900 truncate" title={video.title}>
                  {video.title}
                </h3>
                <p className="text-sm text-gray-500 truncate">
                  {video.fileName}
                </p>
              </div>

              <div className="flex items-center justify-between text-sm text-gray-500">
                <span>{formatFileSize(video.fileSize)}</span>
                <span>{video.duration || '未知时长'}</span>
              </div>

              <div className="flex items-center justify-between">
                <span className={`inline-block px-2 py-1 text-xs rounded-full ${getStatusColor(video.status)}`}>
                  {getStatusText(video.status)}
                </span>

                {onDeleteVideo && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(video.id);
                    }}
                    className="text-red-600 hover:text-red-800 text-sm"
                  >
                    删除
                  </button>
                )}
              </div>

              {/* 进度条 */}
              {video.status === 2 && video.progress !== null && (
                <div className="mt-2">
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full"
                      style={{ width: `${video.progress}%` }}
                    />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">{video.progress}%</p>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default VideoList;