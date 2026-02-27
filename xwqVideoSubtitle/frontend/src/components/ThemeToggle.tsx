import React from 'react';
import { useTheme } from './Theme';

const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      onClick={toggleTheme}
      style={styles.button}
      title={theme === 'light' ? '切换到暗色模式' : '切换到亮色模式'}
    >
      {theme === 'light' ? '🌙' : '☀️'}
    </button>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  button: {
    padding: '8px 12px',
    fontSize: '20px',
    border: 'none',
    background: 'var(--bg-secondary)',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: 'all 0.3s ease',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
};

export default ThemeToggle;
