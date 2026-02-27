import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, useNavigate, Link } from 'react-router-dom';
import SubtitleDisplay from './components/SubtitleDisplay';
import VideoList from './components/VideoList';
import VideoUpload from './components/VideoUpload';
import ProtectedRoute from './components/ProtectedRoute';
import ThemeToggle from './components/ThemeToggle';
import UserDropdown from './components/UserDropdown';
import { FadeIn } from './components/Animations';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import ForgotPassword from './pages/ForgotPassword';
import { videoApi, authApi } from './services/api';
import { useAppStore } from './store';
import type { Video } from './types';

function MainApp() {
  const navigate = useNavigate();
  const {
    videos,
    setVideos,
    currentVideo,
    setCurrentVideo,
    setError,
    user,
    setUser,
    logout,
    token
  } = useAppStore();
  const [activeTab, setActiveTab] = useState<'upload' | 'list' | 'subtitles'>('upload');

  useEffect(() => {
    // 如果已登录，加载用户信息和视频列表
    if (token) {
      loadUserInfo();
      loadVideos();
    }
  }, [token]);

  const loadUserInfo = async () => {
    try {
      const userInfo = await authApi.getCurrentUser();
      setUser(userInfo);
    } catch (error) {
      // Token 无效，清除登录状态
      logout();
      navigate('/login');
    }
  };

  const loadVideos = async () => {
    try {
      const response = await videoApi.getList();
      setVideos(response);
    } catch (error: any) {
      setError(error.message || '加载视频列表失败');
    }
  };

  const handleVideoSelect = (video: Video) => {
    setCurrentVideo(video);
    setActiveTab('subtitles');
  };

  const handleDeleteVideo = (id: number) => {
    setVideos(videos.filter(v => v.id !== id));
    if (currentVideo?.id === id) {
      setCurrentVideo(null);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 头部导航 */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <h1 className="text-xl font-bold text-gray-900">视频字幕生成系统</h1>

            <div className="flex items-center space-x-4">
              <nav className="flex space-x-4">
                <button
                  onClick={() => setActiveTab('upload')}
                  className={`
                    px-3 py-2 rounded-md text-sm font-medium
                    ${activeTab === 'upload' ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:text-gray-900'}
                  `}
                >
                  上传视频
                </button>
                <button
                  onClick={() => setActiveTab('list')}
                  className={`
                    px-3 py-2 rounded-md text-sm font-medium
                    ${activeTab === 'list' ? 'bg-blue-100 text-blue-700' : 'text-gray-700 hover:text-gray-900'}
                  `}
                >
                  视频列表
                </button>
                <button
                  onClick={() => setActiveTab('subtitles')}
                  disabled={!currentVideo}
                  className={`
                    px-3 py-2 rounded-md text-sm font-medium
                    ${activeTab === 'subtitles' && currentVideo ? 'bg-blue-100 text-blue-700' :
                      currentVideo ? 'text-gray-700 hover:text-gray-900' : 'text-gray-400 cursor-not-allowed'}
                  `}
                >
                  字幕管理
                </button>
              </nav>

              {/* 用户信息 */}
              <div className="flex items-center space-x-3 border-l pl-4">
                <ThemeToggle />
                <UserDropdown />
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* 主要内容 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'upload' && <VideoUpload />}

        {activeTab === 'list' && (
          <VideoList
            videos={videos}
            onVideoSelect={handleVideoSelect}
            onDeleteVideo={handleDeleteVideo}
          />
        )}

        {activeTab === 'subtitles' && <SubtitleDisplay />}
      </main>

      {/* 底部 */}
      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <p className="text-center text-sm text-gray-500">
            © 2024 视频字幕生成系统 - 基于Spring Boot + React构建
          </p>
        </div>
      </footer>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainApp />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
