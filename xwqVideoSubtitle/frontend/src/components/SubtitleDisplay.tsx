import React, { useEffect, useState } from 'react';
import { subtitleApi, systemApi } from '../services/api';
import { useAppStore } from '../store';
import type { LanguageOption, Subtitle, SubtitleSegment, TaskRecord } from '../types';

const SubtitleDisplay: React.FC = () => {
  const { currentVideo, currentSubtitles, updateTask } = useAppStore();
  const [subtitles, setSubtitles] = useState<Subtitle[]>([]);
  const [selectedSubtitle, setSelectedSubtitle] = useState<Subtitle | null>(null);
  const [segments, setSegments] = useState<SubtitleSegment[]>([]);
  const [currentSegmentIndex, setCurrentSegmentIndex] = useState(-1);
  const [languages] = useState<LanguageOption[]>(systemApi.getLanguages());
  const [isPlaying, setIsPlaying] = useState(false);

  // 任务进度相关状态
  const [isGenerating, setIsGenerating] = useState(false);
  const [taskProgress, setTaskProgress] = useState(0);
  const [taskMessage, setTaskMessage] = useState('');
  const [currentTaskId, setCurrentTaskId] = useState<string | null>(null);

  useEffect(() => {
    if (currentVideo) {
      loadSubtitles();
    }
  }, [currentVideo]);

  useEffect(() => {
    if (selectedSubtitle) {
      const segments = JSON.parse(selectedSubtitle.content);
      setSegments(segments || []);
      setCurrentSegmentIndex(-1);
    }
  }, [selectedSubtitle]);

  const loadSubtitles = async () => {
    if (!currentVideo) return;

    try {
      const response = await subtitleApi.getByVideoId(currentVideo.id);
      setSubtitles(response);

      // 如果有字幕，默认选择第一个
      if (response.length > 0) {
        setSelectedSubtitle(response[0]);
      }
    } catch (error) {
      console.error('加载字幕失败:', error);
    }
  };

  const generateSubtitle = async () => {
    if (!currentVideo) return;

    try {
      setIsGenerating(true);
      setTaskProgress(0);
      setTaskMessage('正在启动字幕生成任务...');

      // 使用 'auto' 让后端自动检测语言
      const taskId = await subtitleApi.generate(currentVideo.id, 'auto');
      setCurrentTaskId(taskId);

      // 定期检查任务状态
      const checkStatus = setInterval(async () => {
        try {
          const task: TaskRecord = await subtitleApi.getTaskStatus(taskId);
          updateTask(taskId, task);

          // 更新进度和消息
          setTaskProgress(task.progress || 0);

          // 显示后端返回的消息，如果没有则显示默认消息
          if (task.message) {
            setTaskMessage(task.message);
          } else if (task.progress < 100) {
            setTaskMessage('处理中...');
          }

          if (task.status === 1) { // 完成
            clearInterval(checkStatus);
            setIsGenerating(false);
            setTaskProgress(100);
            setTaskMessage('字幕生成完成！');
            loadSubtitles(); // 重新加载字幕

            // 3秒后清除提示
            setTimeout(() => {
              setTaskMessage('');
              setCurrentTaskId(null);
            }, 3000);
          } else if (task.status === 2) { // 失败
            clearInterval(checkStatus);
            setIsGenerating(false);
            // 优先显示errorMessage，其次显示message
            const errorMsg = task.errorMessage || task.message || '未知错误';
            setTaskMessage(errorMsg);
          }
        } catch (error: any) {
          console.error('检查任务状态失败:', error);
          clearInterval(checkStatus);
          setIsGenerating(false);
          // 显示后端返回的错误信息
          const errorMsg = error?.response?.data?.message || error?.message || '检查任务状态失败';
          setTaskMessage(errorMsg);
        }
      }, 2000);
    } catch (error: any) {
      console.error('启动字幕生成失败:', error);
      setIsGenerating(false);
      // 显示后端返回的错误信息
      const errorMsg = error?.response?.data?.message || error?.message || '启动字幕生成失败';
      setTaskMessage(errorMsg);
    }
  };

  const exportSubtitle = (format = 'srt') => {
    if (!selectedSubtitle) return;

    subtitleApi.exportFile(selectedSubtitle.id, format)
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${currentVideo?.title || 'subtitle'}.${format}`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      })
      .catch(error => {
        console.error('导出失败:', error);
      });
  };

  if (!currentVideo) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <p className="text-gray-500">请先选择一个视频</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-2xl font-bold">字幕管理</h2>

        <div className="flex items-center gap-3">
          {/* 生成字幕按钮 */}
          <button
            onClick={generateSubtitle}
            disabled={isGenerating}
            className={`
              px-4 py-2 rounded-md transition-colors whitespace-nowrap
              ${isGenerating
                ? 'bg-gray-400 text-gray-200 cursor-not-allowed'
                : 'bg-green-600 text-white hover:bg-green-700'}
            `}
          >
            {isGenerating ? '生成中...' : '生成字幕'}
          </button>
        </div>
      </div>

      {/* 进度条 */}
      {isGenerating && (
        <div className="mb-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm font-medium text-blue-900">字幕生成进度</span>
            <span className="text-sm font-bold text-blue-600">{taskProgress}%</span>
          </div>
          <div className="w-full bg-blue-200 rounded-full h-3 overflow-hidden">
            <div
              className="bg-blue-600 h-full rounded-full transition-all duration-500 ease-out"
              style={{ width: `${taskProgress}%` }}
            />
          </div>
          <p className="mt-2 text-sm text-blue-700">{taskMessage}</p>
        </div>
      )}

      {/* 完成提示 */}
      {!isGenerating && taskMessage && taskProgress === 100 && (
        <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-center">
            <svg className="w-5 h-5 text-green-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            <span className="text-green-800 font-medium">{taskMessage}</span>
          </div>
        </div>
      )}

      {/* 错误提示 */}
      {!isGenerating && taskMessage && (taskProgress < 100 || taskMessage.includes('失败') || taskMessage.includes('错误')) && (
        <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-start">
            <svg className="w-5 h-5 text-red-600 mr-2 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
            <div className="flex-1">
              <p className="text-red-800 font-medium mb-1">字幕生成失败</p>
              <p className="text-red-700 text-sm">{taskMessage}</p>
            </div>
            <button
              onClick={() => {
                setTaskMessage('');
                setCurrentTaskId(null);
                setTaskProgress(0);
              }}
              className="text-red-600 hover:text-red-800 ml-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      )}

      {/* 字幕列表 */}
      <div className="mb-6">
        <h3 className="text-lg font-semibold mb-2">已有字幕</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {subtitles.map((subtitle) => (
            <div
              key={subtitle.id}
              className={`
                border rounded-lg p-4 cursor-pointer transition-colors
                ${selectedSubtitle?.id === subtitle.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200'}
              `}
              onClick={() => setSelectedSubtitle(subtitle)}
            >
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-medium">
                    {languages.find(lang => lang.code === subtitle.language)?.name || subtitle.language}
                  </p>
                  <p className="text-sm text-gray-500">
                    {subtitle.segmentCount || 0} 个片段 · {subtitle.duration || 0} 秒
                  </p>
                </div>
                <div className="text-right">
                  <span className={`inline-block px-2 py-1 text-xs rounded-full ${subtitle.status === 1 ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                    }`}>
                    {subtitle.status === 1 ? '完成' : '处理中'}
                  </span>
                </div>
              </div>
              <div className="mt-2 flex gap-2">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    exportSubtitle('srt');
                  }}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  导出SRT
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    exportSubtitle('vtt');
                  }}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  导出VTT
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 字幕预览 */}
      {selectedSubtitle && segments.length > 0 && (
        <div className="mt-6">
          <h3 className="text-lg font-semibold mb-2">字幕预览</h3>
          <div className="border rounded-lg p-4 bg-gray-50 max-h-96 overflow-y-auto">
            {segments.map((segment, index) => (
              <div
                key={index}
                className={`mb-4 p-2 rounded ${currentSegmentIndex === index ? 'bg-blue-100' : ''
                  }`}
              >
                <div className="text-sm text-gray-500 mb-1">
                  {formatTime(segment.startTime)} - {formatTime(segment.endTime)}
                </div>
                <div className="text-gray-800">{segment.text}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 播放器（简单示例） */}
      <div className="mt-6">
        <h3 className="text-lg font-semibold mb-2">视频播放器</h3>
        <div className="bg-gray-100 rounded-lg p-8 text-center">
          <div className="text-4xl mb-4">▶️</div>
          <p className="text-gray-500">视频播放功能将集成在这里</p>
        </div>
      </div>
    </div>
  );
};

// 格式化时间
function formatTime(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);
  const millis = Math.floor((seconds % 1) * 1000);

  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0')}`;
}

export default SubtitleDisplay;