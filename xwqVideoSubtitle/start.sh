#!/bin/bash

echo "================================"
echo "🚀 启动视频字幕生成系统"
echo "================================"
echo ""

# 检查后端是否运行
echo "1️⃣  检查后端服务..."
if lsof -i :8081 > /dev/null 2>&1; then
    echo "✅ 后端服务已在运行 (端口 8081)"
else
    echo "⚠️  后端服务未运行"
    echo "请在 IntelliJ IDEA 中运行 SubtitleGeneratorApplication"
    echo ""
    read -p "按 Enter 键继续..."
fi

echo ""
echo "2️⃣  启动前端服务..."
cd /Users/hewei/myProject/xwqVideoSubtitle/xwqVideoSubtitle/frontend

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装前端依赖..."
    npm install
fi

echo "🌟 启动前端开发服务器..."
echo ""
echo "================================"
echo "✅ 系统启动完成！"
echo "================================"
echo ""
echo "📍 访问地址："
echo "   前端: http://localhost:5173"
echo "   后端: http://localhost:8081/api"
echo ""
echo "👤 默认管理员账户："
echo "   用户名: admin"
echo "   密码: admin123"
echo ""
echo "按 Ctrl+C 停止前端服务"
echo ""

npm run dev
