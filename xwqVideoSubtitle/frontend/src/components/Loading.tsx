import React from 'react';

interface LoadingProps {
  message?: string;
  fullScreen?: boolean;
}

const Loading: React.FC<LoadingProps> = ({ message = '加载中...', fullScreen = false }) => {
  if (fullScreen) {
    return (
      <div style={styles.fullScreenOverlay}>
        <div style={styles.spinnerContainer}>
          <div style={styles.spinner}></div>
          <p style={styles.message}>{message}</p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.inlineContainer}>
      <div style={styles.smallSpinner}></div>
      <span style={styles.inlineMessage}>{message}</span>
    </div>
  );
};

const styles: { [key: string]: React.CSSProperties } = {
  fullScreenOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 9998,
  },
  spinnerContainer: {
    textAlign: 'center',
  },
  spinner: {
    width: '50px',
    height: '50px',
    border: '4px solid #f3f3f3',
    borderTop: '4px solid #4a90e2',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    margin: '0 auto 20px',
  },
  message: {
    color: '#666',
    fontSize: '16px',
    margin: 0,
  },
  inlineContainer: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '10px',
    padding: '20px',
  },
  smallSpinner: {
    width: '20px',
    height: '20px',
    border: '3px solid #f3f3f3',
    borderTop: '3px solid #4a90e2',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
  },
  inlineMessage: {
    color: '#666',
    fontSize: '14px',
  },
};

// 添加 CSS 动画
const styleSheet = document.createElement('style');
styleSheet.textContent = `
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`;
document.head.appendChild(styleSheet);

export default Loading;
