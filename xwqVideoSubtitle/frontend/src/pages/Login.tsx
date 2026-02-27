import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FadeIn, SlideIn } from '../components/Animations';
import ThemeToggle from '../components/ThemeToggle';
import { useToast } from '../components/Toast';
import { authApi } from '../services/api';
import { useAppStore } from '../store';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const login = useAppStore((state) => state.login);
  const { showToast } = useToast();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 组件加载时，从 localStorage 读取记住的用户名和密码
  useEffect(() => {
    const savedUsername = localStorage.getItem('rememberedUsername');
    const savedPassword = localStorage.getItem('rememberedPassword');
    const isRemembered = localStorage.getItem('isRemembered') === 'true';

    if (isRemembered && savedUsername && savedPassword) {
      setUsername(savedUsername);
      setPassword(savedPassword);
      setRememberMe(true);
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // 验证
    if (!username.trim()) {
      setError('请输入用户名');
      return;
    }
    if (!password.trim()) {
      setError('请输入密码');
      return;
    }

    setLoading(true);
    try {
      const response = await authApi.login(username, password);

      // 处理记住密码
      if (rememberMe) {
        localStorage.setItem('rememberedUsername', username);
        localStorage.setItem('rememberedPassword', password);
        localStorage.setItem('isRemembered', 'true');
      } else {
        localStorage.removeItem('rememberedUsername');
        localStorage.removeItem('rememberedPassword');
        localStorage.setItem('isRemembered', 'false');
      }

      // 先保存 token 和用户信息到状态
      login(response.token, response.user);

      showToast('登录成功！', 'success');

      // 使用 requestAnimationFrame 确保状态更新后再跳转
      requestAnimationFrame(() => {
        navigate('/', { replace: true });
      });
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || '登录失败，请检查用户名和密码';
      setError(errorMsg);
      showToast(errorMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.themeToggle}>
        <ThemeToggle />
      </div>

      <FadeIn>
        <div style={styles.form}>
          <SlideIn direction="down">
            <h1 style={styles.title}>🎬 视频字幕生成系统</h1>
            <h2 style={styles.subtitle}>用户登录</h2>
          </SlideIn>

          <form onSubmit={handleSubmit}>
            <SlideIn direction="up" delay={0.1}>
              <div style={styles.field}>
                <label style={styles.label}>用户名</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="请输入用户名"
                  style={styles.input}
                  disabled={loading}
                />
              </div>
            </SlideIn>

            <SlideIn direction="up" delay={0.2}>
              <div style={styles.field}>
                <label style={styles.label}>密码</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="请输入密码"
                  style={styles.input}
                  disabled={loading}
                />
              </div>
            </SlideIn>

            <SlideIn direction="up" delay={0.3}>
              <div style={styles.rememberMe}>
                <input
                  type="checkbox"
                  id="rememberMe"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  disabled={loading}
                  style={styles.checkbox}
                />
                <label htmlFor="rememberMe" style={styles.checkboxLabel}>
                  记住密码
                </label>
              </div>
            </SlideIn>

            {error && (
              <SlideIn direction="up">
                <div style={styles.error}>{error}</div>
              </SlideIn>
            )}

            <SlideIn direction="up" delay={0.4}>
              <button
                type="submit"
                style={{ ...styles.button, ...(loading ? styles.buttonDisabled : {}) }}
                disabled={loading}
              >
                {loading ? '登录中...' : '登录'}
              </button>
            </SlideIn>
          </form>

          <SlideIn direction="up" delay={0.5}>
            <div style={styles.footer}>
              还没有账号？{' '}
              <Link to="/register" style={styles.link}>
                立即注册
              </Link>
              <span style={styles.divider}>|</span>
              <Link to="/forgot-password" style={styles.link}>
                忘记密码
              </Link>
            </div>
          </SlideIn>
        </div>
      </FadeIn>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    backgroundColor: 'var(--bg-secondary)',
    position: 'relative',
  },
  themeToggle: {
    position: 'absolute',
    top: '20px',
    right: '20px',
  },
  form: {
    backgroundColor: 'var(--bg-primary)',
    padding: '40px',
    borderRadius: '16px',
    boxShadow: '0 4px 20px var(--shadow)',
    width: '100%',
    maxWidth: '400px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '8px',
    textAlign: 'center',
    color: 'var(--text-primary)',
  },
  subtitle: {
    fontSize: '16px',
    color: 'var(--text-secondary)',
    marginBottom: '32px',
    textAlign: 'center',
  },
  field: {
    marginBottom: '20px',
  },
  label: {
    display: 'block',
    marginBottom: '8px',
    fontSize: '14px',
    fontWeight: '500',
    color: 'var(--text-primary)',
  },
  input: {
    width: '100%',
    padding: '12px',
    fontSize: '14px',
    border: '1px solid var(--border-color)',
    borderRadius: '8px',
    outline: 'none',
    boxSizing: 'border-box',
    backgroundColor: 'var(--bg-primary)',
    color: 'var(--text-primary)',
  },
  error: {
    padding: '12px',
    marginBottom: '16px',
    backgroundColor: '#fff2f0',
    color: '#ff4d4f',
    borderRadius: '8px',
    fontSize: '14px',
  },
  button: {
    width: '100%',
    padding: '12px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#4a90e2',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    marginBottom: '16px',
    transition: 'all 0.3s ease',
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  footer: {
    textAlign: 'center',
    fontSize: '14px',
    color: 'var(--text-secondary)',
  },
  link: {
    color: '#4a90e2',
    textDecoration: 'none',
    fontWeight: '500',
  },
  divider: {
    margin: '0 8px',
    color: 'var(--text-secondary)',
  },
  rememberMe: {
    display: 'flex',
    alignItems: 'center',
    marginBottom: '16px',
  },
  checkbox: {
    marginRight: '8px',
    cursor: 'pointer',
  },
  checkboxLabel: {
    fontSize: '14px',
    color: '#666',
    cursor: 'pointer',
    userSelect: 'none',
  },
};

export default Login;
