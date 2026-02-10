#!/bin/bash

# xwqVideoSubtitle 视频字幕生成系统启动脚本

echo "=== xwqVideoSubtitle 启动脚本 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK 21"
    exit 1
fi

java -version

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请安装Maven"
    exit 1
fi

echo "Maven版本: $(mvn -version)"

# 编译和启动后端
echo "正在编译后端项目..."
cd backend

# 确保本地仓库配置正确
if [ -n "$MAVEN_HOME" ]; then
    echo "使用Maven仓库: $MAVEN_HOME"
    export MAVEN_OPTS="-Dmaven.repo.local=$MAVEN_HOME/repository"
fi

# 编译项目
mvn clean compile -DskipTests

echo "正在启动后端服务..."
mvn spring-boot:run &
BACKEND_PID=$!

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 检查后端是否启动成功
if curl -f http://localhost:8080/api/actuator/health &>/dev/null; then
    echo "后端服务已启动，PID: $BACKEND_PID"
else
    echo "后端服务启动失败，请检查日志"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# 启动前端
echo "正在启动前端服务..."
cd ../frontend

# 安装依赖（如果需要）
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

echo "启动前端开发服务器..."
npm run dev &
FRONTEND_PID=$!

echo "前端服务已启动，PID: $FRONTEND_PID"

echo ""
echo "=== 服务启动成功 ==="
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:5173"
echo "API文档: http://localhost:8080/api/swagger-ui/index.html"
echo ""
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
trap 'echo "正在停止服务..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit' INT
wait

echo "服务已停止"