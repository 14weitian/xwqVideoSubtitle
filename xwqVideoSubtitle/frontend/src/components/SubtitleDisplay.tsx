import React, { useState, useEffect } from 'react';
import { subtitleApi, systemApi } from '../services/api';
import { useAppStore } from '../store';
import type { Subtitle, SubtitleSegment, LanguageOption } from '../types';

const SubtitleDisplay: React.FC = () => {
  const { currentVideo, currentSubtitles, updateTask } = useAppStore();
  const [subtitles, setSubtitles] = useState<Subtitle[]>([]);
  const [selectedSubtitle, setSelectedSubtitle] = useState<Subtitle | null>(null);
  const [segments, setSegments] = useState<SubtitleSegment[]>([]);
  const [currentSegmentIndex, setCurrentSegmentIndex] = useState(-1);
  const [languages] = useState<LanguageOption[]>(systemApi.getLanguages());
  const [isPlaying, setIsPlaying] = useState(false);

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
      const taskId = await subtitleApi.generate(currentVideo.id, 'zh-CN');
      console.log('生成任务已启动，任务ID:', taskId);

      // 定期检查任务状态
      const checkStatus = setInterval(async () => {
        try {
          const task = await subtitleApi.getTaskStatus(taskId);
          updateTask(taskId, task);

          if (task.status === 1) { // 完成
            clearInterval(checkStatus);
            loadSubtitles(); // 重新加载字幕
          } else if (task.status === 2) { // 失败
            clearInterval(checkStatus);
            console.error('字幕生成失败:', task.errorMessage);
          }
        } catch (error) {
          console.error('检查任务状态失败:', error);
        }
      }, 2000);
    } catch (error) {
      console.error('启动字幕生成失败:', error);
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
        <button
          onClick={generateSubtitle}
          className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition-colors"
        >
          生成字幕
        </button>
      </div>

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
                  <span className={`inline-block px-2 py-1 text-xs rounded-full ${
                    subtitle.status === 1 ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
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
                className={`mb-4 p-2 rounded ${
                  currentSegmentIndex === index ? 'bg-blue-100' : ''
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