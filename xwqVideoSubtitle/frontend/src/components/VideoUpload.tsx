import { useRef, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { videoApi } from '../services/api';
import { useAppStore } from '../store';

const VideoUpload: React.FC = () => {
  const [title, setTitle] = useState('');
  const [language, setLanguage] = useState('zh-CN');
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { addVideo, setUploading, setError, uploading } = useAppStore();

  const onDrop = async (acceptedFiles: File[]) => {
    if (acceptedFiles.length === 0) return;

    const file = acceptedFiles[0];
    setUploading(true);
    setError(null);

    try {
      // 验证文件格式
      await videoApi.validateFormat(file.name);

      // 上传文件
      const response = await videoApi.upload(file, title || undefined, language);

      // 添加到列表
      addVideo(response);

      // 重置表单
      setTitle('');

    } catch (error: any) {
      setError(error.message || '上传失败');
    } finally {
      setUploading(false);
    }
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'video/*': ['.mp4', '.avi', '.mkv', '.mov', '.wmv', '.flv']
    },
    maxFiles: 1,
    multiple: false,
  });

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <h2 className="text-2xl font-bold mb-4">视频上传</h2>

      <div
        {...getRootProps()}
        onClick={handleClick}
        className={`
          border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
          ${isDragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}
          ${isDragging ? 'border-blue-500 bg-blue-50' : ''}
        `}
      >
        <input
          {...getInputProps()}
          ref={fileInputRef}
          type="file"
          accept="video/*"
        />

        <div className="space-y-4">
          <div className="text-6xl">📹</div>
          <div>
            <p className="text-lg font-medium">
              {isDragActive ? '拖放视频文件到这里' : '拖放视频文件到这里，或点击选择'}
            </p>
            <p className="text-sm text-gray-500 mt-2">
              支持格式：MP4, AVI, MKV, MOV, WMV, FLV（最大500MB）
            </p>
          </div>
        </div>
      </div>

      {/* 上传选项 */}
      <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            视频标题（可选）
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="请输入视频标题"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            字幕语言
          </label>
          <select
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="zh-CN">中文（简体）</option>
            <option value="zh-TW">中文（繁体）</option>
            <option value="en-US">英语</option>
            <option value="ja-JP">日语</option>
            <option value="ko-KR">韩语</option>
          </select>
        </div>

        <div className="flex items-end">
          <button
            onClick={handleClick}
            disabled={isDragging}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {uploading ? '上传中...' : '选择视频'}
          </button>
        </div>
      </div>

      {/* 错误提示 */}
      {useAppStore.getState().error && (
        <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
          <p className="text-red-600">{useAppStore.getState().error}</p>
        </div>
      )}
    </div>
  );
};

export default VideoUpload;