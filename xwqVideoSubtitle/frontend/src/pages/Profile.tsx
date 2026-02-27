import React, { useState, useRef } from 'react';
import { useAppStore } from '../store';
import { authApi } from '../services/api';
import { useToast } from '../components/Toast';

const Profile: React.FC = () => {
  const { user, setUser } = useAppStore();
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState<'info' | 'password'>('info');
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 更新信息的表单
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [email, setEmail] = useState(user?.email || '');
  const [updating, setUpdating] = useState(false);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);

  // 修改密码的表单
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [changingPassword, setChangingPassword] = useState(false);

  // 密码强度检测
  const checkPasswordStrength = (password: string): { level: string; color: string } => {
    if (password.length < 6) return { level: '弱', color: '#ff4d4f' };
    if (password.length < 8) return { level: '中', color: '#faad14' };
    if (password.length < 12) return { level: '强', color: '#52c41a' };
    return { level: '非常强', color: '#1890ff' };
  };

  // 获取用户头像首字母
  const getInitials = () => {
    if (user?.nickname) {
      return user.nickname.charAt(0).toUpperCase();
    }
    if (user?.username) {
      return user.username.charAt(0).toUpperCase();
    }
    return 'U';
  };

  // 头像上传处理
  const handleAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      showToast('只能上传图片文件', 'error');
      return;
    }

    // 验证文件大小（最大 2MB）
    if (file.size > 2 * 1024 * 1024) {
      showToast('图片大小不能超过 2MB', 'error');
      return;
    }

    setUploadingAvatar(true);
    try {
      const updatedUser = await authApi.uploadAvatar(file);
      setUser(updatedUser);
      showToast('头像上传成功！', 'success');
    } catch (error: any) {
      showToast(error.response?.data?.message || error.message || '上传失败', 'error');
    } finally {
      setUploadingAvatar(false);
      // 清空 input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setUpdating(true);

    try {
      const updatedUser = await authApi.updateProfile(nickname, email);
      setUser(updatedUser);
      showToast('信息更新成功！', 'success');
    } catch (error: any) {
      showToast(error.response?.data?.message || error.message || '更新失败', 'error');
    } finally {
      setUpdating(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    // 前端验证
    if (!oldPassword.trim()) {
      showToast('请输入旧密码', 'error');
      return;
    }
    if (newPassword.length < 6) {
      showToast('新密码长度至少为6位', 'error');
      return;
    }
    if (newPassword !== confirmPassword) {
      showToast('两次输入的新密码不一致', 'error');
      return;
    }

    setChangingPassword(true);

    try {
      await authApi.changePassword(oldPassword, newPassword, confirmPassword);
      showToast('密码修改成功！', 'success');
      // 清空表单
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error: any) {
      showToast(error.response?.data?.message || error.message || '修改失败', 'error');
    } finally {
      setChangingPassword(false);
    }
  };

  const passwordStrength = checkPasswordStrength(newPassword);

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>个人信息</h1>

        {/* 选项卡 */}
        <div style={styles.tabs}>
          <button
            style={{
              ...styles.tab,
              ...(activeTab === 'info' ? styles.activeTab : {}),
            }}
            onClick={() => setActiveTab('info')}
          >
            基本信息
          </button>
          <button
            style={{
              ...styles.tab,
              ...(activeTab === 'password' ? styles.activeTab : {}),
            }}
            onClick={() => setActiveTab('password')}
          >
            修改密码
          </button>
        </div>

        {/* 基本信息 */}
        {activeTab === 'info' && (
          <form onSubmit={handleUpdateProfile} style={styles.form}>
            {/* 头像上传 */}
            <div style={styles.avatarSection}>
              <div
                style={{
                  ...styles.avatar,
                  background: user?.avatar
                    ? `url(http://localhost:8081/api${user.avatar}) center/cover`
                    : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                }}
              >
                {user?.avatar ? '' : getInitials()}
              </div>
              <button
                type="button"
                onClick={handleAvatarClick}
                style={{ ...styles.avatarButton, ...(uploadingAvatar ? styles.buttonDisabled : {}) }}
                disabled={uploadingAvatar}
              >
                {uploadingAvatar ? '上传中...' : '更换头像'}
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleAvatarChange}
                style={{ display: 'none' }}
              />
              <div style={styles.avatarHint}>支持 JPG、PNG 格式，大小不超过 2MB</div>
            </div>

            <div style={styles.field}>
              <label style={styles.label}>用户名</label>
              <input
                type="text"
                value={user?.username || ''}
                disabled
                style={{ ...styles.input, ...styles.disabledInput }}
              />
              <span style={styles.hint}>用户名不可修改</span>
            </div>

            <div style={styles.field}>
              <label style={styles.label}>昵称</label>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="请输入昵称"
                style={styles.input}
                disabled={updating}
              />
            </div>

            <div style={styles.field}>
              <label style={styles.label}>邮箱</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="请输入邮箱"
                style={styles.input}
                disabled={updating}
              />
            </div>

            <button
              type="submit"
              style={{ ...styles.button, ...(updating ? styles.buttonDisabled : {}) }}
              disabled={updating}
            >
              {updating ? '保存中...' : '保存修改'}
            </button>
          </form>
        )}

        {/* 修改密码 */}
        {activeTab === 'password' && (
          <form onSubmit={handleChangePassword} style={styles.form}>
            <div style={styles.field}>
              <label style={styles.label}>旧密码</label>
              <input
                type="password"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                placeholder="请输入旧密码"
                style={styles.input}
                disabled={changingPassword}
              />
            </div>

            <div style={styles.field}>
              <label style={styles.label}>新密码</label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="请输入新密码（至少6位）"
                style={styles.input}
                disabled={changingPassword}
              />
              {newPassword && (
                <div style={{ ...styles.strength, color: passwordStrength.color }}>
                  密码强度：{passwordStrength.level}
                </div>
              )}
            </div>

            <div style={styles.field}>
              <label style={styles.label}>确认新密码</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="请再次输入新密码"
                style={styles.input}
                disabled={changingPassword}
              />
              {confirmPassword && newPassword !== confirmPassword && (
                <span style={styles.errorHint}>两次输入的密码不一致</span>
              )}
            </div>

            <button
              type="submit"
              style={{ ...styles.button, ...(changingPassword ? styles.buttonDisabled : {}) }}
              disabled={changingPassword}
            >
              {changingPassword ? '修改中...' : '修改密码'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  container: {
    maxWidth: '800px',
    margin: '0 auto',
    padding: '40px 20px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
    padding: '40px',
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '24px',
    color: '#333',
  },
  tabs: {
    display: 'flex',
    borderBottom: '2px solid #f0f0f0',
    marginBottom: '24px',
  },
  tab: {
    padding: '12px 24px',
    fontSize: '16px',
    border: 'none',
    background: 'none',
    cursor: 'pointer',
    color: '#666',
    borderBottom: '2px solid transparent',
    marginBottom: '-2px',
  },
  activeTab: {
    color: '#4a90e2',
    borderBottomColor: '#4a90e2',
    fontWeight: 'bold',
  },
  form: {
    marginTop: '24px',
  },
  field: {
    marginBottom: '20px',
  },
  label: {
    display: 'block',
    marginBottom: '8px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#333',
  },
  input: {
    width: '100%',
    padding: '12px',
    fontSize: '14px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    outline: 'none',
    boxSizing: 'border-box',
  },
  disabledInput: {
    backgroundColor: '#f5f5f5',
    cursor: 'not-allowed',
  },
  hint: {
    display: 'block',
    marginTop: '4px',
    fontSize: '12px',
    color: '#999',
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
    borderRadius: '4px',
    cursor: 'pointer',
    marginTop: '8px',
  },
  buttonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  avatarSection: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    marginBottom: '32px',
    paddingBottom: '32px',
    borderBottom: '1px solid #f0f0f0',
  },
  avatar: {
    width: '100px',
    height: '100px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '40px',
    color: 'white',
    fontWeight: 'bold',
    marginBottom: '16px',
    cursor: 'pointer',
    transition: 'transform 0.3s ease',
  },
  avatarButton: {
    padding: '8px 24px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#4a90e2',
    backgroundColor: 'white',
    border: '1px solid #4a90e2',
    borderRadius: '4px',
    cursor: 'pointer',
    transition: 'all 0.3s ease',
  },
  avatarHint: {
    marginTop: '8px',
    fontSize: '12px',
    color: '#999',
  },
};

export default Profile;
