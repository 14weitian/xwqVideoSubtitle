import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../services/api';
import { useToast } from '../components/Toast';
import { FadeIn, SlideIn } from '../components/Animations';
import ThemeToggle from '../components/ThemeToggle';

const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [step, setStep] = useState<'email' | 'code' | 'success'>('email');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);

  // 密码强度检测
  const checkPasswordStrength = (password: string): { level: string; color: string } => {
    if (password.length < 6) return { level: '弱', color: '#ff4d4f' };
    if (password.length < 8) return { level: '中', color: '#faad14' };
    if (password.length < 12) return { level: '强', color: '#52c41a' };
    return { level: '非常强', color: '#1890ff' };
  };

  // 发送验证码
  const handleSendCode = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email.trim()) {
      showToast('请输入邮箱地址', 'error');
      return;
    }

    // 验证邮箱格式
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
    if (!emailRegex.test(email)) {
      showToast('邮箱格式不正确', 'error');
      return;
    }

    setLoading(true);
    try {
      await authApi.sendResetCode(email);
      showToast('验证码已发送到您的邮箱', 'success');
      setStep('code');
      // 开始倒计时
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error: any) {
      showToast(error.response?.data?.message || error.message || '发送失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  // 重置密码
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!code.trim()) {
      showToast('请输入验证码', 'error');
      return;
    }
    if (newPassword.length < 6) {
      showToast('密码长度至少为6位', 'error');
      return;
    }
    if (newPassword !== confirmPassword) {
      showToast('两次输入的密码不一致', 'error');
      return;
    }

    setLoading(true);
    try {
      await authApi.resetPassword(email, code, newPassword, confirmPassword);
      showToast('密码重置成功！', 'success');
      setStep('success');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (error: any) {
      showToast(error.response?.data?.message || error.message || '重置失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const passwordStrength = checkPasswordStrength(newPassword);

  return (
    <div style={styles.container}>
      <div style={styles.themeToggle}>
        <ThemeToggle />
      </div>

      <FadeIn>
        <div style={styles.form}>
          <SlideIn direction="down">
            <h1 style={styles.title}>🎬 忘记密码</h1>
            <p style={styles.subtitle}>
              {step === 'email' && '请输入您的注册邮箱'}
              {step === 'code' && '验证码已发送到您的邮箱'}
              {step === 'success' && '密码重置成功'}
            </p>
          </SlideIn>

          {step === 'email' && (
            <form onSubmit={handleSendCode}>
              <SlideIn direction="up" delay={0.1}>
                <div style={styles.field}>
                  <label style={styles.label}>邮箱地址</label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="请输入注册时使用的邮箱"
                    style={styles.input}
                    disabled={loading}
                  />
                </div>
              </SlideIn>

              <SlideIn direction="up" delay={0.2}>
                <button
                  type="submit"
                  style={{ ...styles.button, ...(loading ? styles.buttonDisabled : {}) }}
                  disabled={loading}
                >
                  {loading ? '发送中...' : '发送验证码'}
                </button>
              </SlideIn>

              <SlideIn direction="up" delay={0.3}>
                <div style={styles.footer}>
                  想起密码了？{' '}
                  <Link to="/login" style={styles.link}>
                    返回登录
                  </Link>
                </div>
              </SlideIn>
            </form>
          )}

          {step === 'code' && (
            <form onSubmit={handleResetPassword}>
              <SlideIn direction="up" delay={0.1}>
                <div style={styles.field}>
                  <label style={styles.label}>验证码</label>
                  <div style={styles.codeInputWrapper}>
                    <input
                      type="text"
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      placeholder="请输入6位验证码"
                      style={{ ...styles.input, ...styles.codeInput }}
                      disabled={loading}
                      maxLength={6}
                    />
                    <button
                      type="button"
                      onClick={handleSendCode}
                      style={{ ...styles.resendButton, ...(countdown > 0 || loading ? styles.buttonDisabled : {}) }}
                      disabled={countdown > 0 || loading}
                    >
                      {countdown > 0 ? `${countdown}s` : '重新发送'}
                    </button>
                  </div>
                </div>
              </SlideIn>

              <SlideIn direction="up" delay={0.2}>
                <div style={styles.field}>
                  <label style={styles.label}>新密码</label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="请输入新密码（至少6位）"
                    style={styles.input}
                    disabled={loading}
                  />
                  {newPassword && (
                    <div style={{ ...styles.strength, color: passwordStrength.color }}>
                      密码强度：{passwordStrength.level}
                    </div>
                  )}
                </div>
              </SlideIn>

              <SlideIn direction="up" delay={0.3}>
                <div style={styles.field}>
                  <label style={styles.label}>确认新密码</label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="请再次输入新密码"
                    style={styles.input}
                    disabled={loading}
                  />
                  {confirmPassword && newPassword !== confirmPassword && (
                    <span style={styles.errorHint}>两次输入的密码不一致</span>
                  )}
                </div>
              </SlideIn>

              <SlideIn direction="up" delay={0.4}>
                <button
                  type="submit"
                  style={{ ...styles.button, ...(loading ? styles.buttonDisabled : {}) }}
                  disabled={loading}
                >
                  {loading ? '重置中...' : '重置密码'}
                </button>
              </SlideIn>

              <SlideIn direction="up" delay={0.5}>
                <div style={styles.footer}>
                  <Link to="/login" style={styles.link}>
                    返回登录
                  </Link>
                </div>
              </SlideIn>
            </form>
          )}

          {step === 'success' && (
            <SlideIn direction="up">
              <div style={styles.successBox}>
                <div style={styles.successIcon}>✓</div>
                <p style={styles.successText}>密码重置成功！</p>
                <p style={styles.successHint}>即将跳转到登录页面...</p>
              </div>
            </SlideIn>
          )}
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
    fontSize: '14px',
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
  codeInputWrapper: {
    display: 'flex',
    gap: '10px',
  },
  codeInput: {
    flex: 1,
  },
  resendButton: {
    padding: '0 16px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#4a90e2',
    backgroundColor: 'white',
    border: '1px solid #4a90e2',
    borderRadius: '8px',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    transition: 'all 0.3s ease',
  },
  errorHint: {
    display: 'block',
    marginTop: '4px',
    fontSize: '12px',
    color: '#ff4d4f',
  },
  strength: {
    marginTop: '4px',
    fontSize: '12px',
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
  successBox: {
    textAlign: 'center',
    padding: '40px 20px',
  },
  successIcon: {
    width: '80px',
    height: '80px',
    margin: '0 auto 20px',
    borderRadius: '50%',
    backgroundColor: '#52c41a',
    color: 'white',
    fontSize: '48px',
    fontWeight: 'bold',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  successText: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: 'var(--text-primary)',
    marginBottom: '12px',
  },
  successHint: {
    fontSize: '14px',
    color: 'var(--text-secondary)',
  },
};

export default ForgotPassword;
