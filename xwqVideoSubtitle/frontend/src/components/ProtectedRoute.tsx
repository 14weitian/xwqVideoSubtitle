import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAppStore } from '../store';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const location = useLocation();
  const isAuthenticated = useAppStore((state) => state.isAuthenticated);
  const token = useAppStore((state) => state.token);

  useEffect(() => {
    console.log('ProtectedRoute 检查:', {
      path: location.pathname,
      isAuthenticated,
      hasToken: !!token
    });
  }, [isAuthenticated, token, location.pathname]);

  // 检查是否有 token 或认证状态
  if (!isAuthenticated && !token) {
    //console.log('未认证，跳转到登录页');
    // 未登录，重定向到登录页面
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  //console.log('认证通过，渲染受保护内容');
  return <>{children}</>;
};

export default ProtectedRoute;
