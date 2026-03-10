#!/bin/bash
#=============================================
# CPA Daily - 停止应用脚本
#=============================================

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo -e "${GREEN}[INFO]${NC} 停止 CPA Daily 应用..."

# 停止后端
if [ -f "$PROJECT_DIR/logs/backend.pid" ]; then
    BACKEND_PID=$(cat "$PROJECT_DIR/logs/backend.pid")
    if kill -0 "$BACKEND_PID" 2>/dev/null; then
        kill "$BACKEND_PID"
        echo -e "${GREEN}[INFO]${NC} 后端已停止 (PID: $BACKEND_PID)"
    fi
    rm -f "$PROJECT_DIR/logs/backend.pid"
fi

# 停止前端
if [ -f "$PROJECT_DIR/logs/frontend.pid" ]; then
    FRONTEND_PID=$(cat "$PROJECT_DIR/logs/frontend.pid")
    if kill -0 "$FRONTEND_PID" 2>/dev/null; then
        kill "$FRONTEND_PID"
        echo -e "${GREEN}[INFO]${NC} 前端已停止 (PID: $FRONTEND_PID)"
    fi
    rm -f "$PROJECT_DIR/logs/frontend.pid"
fi

# 兜底: 杀掉所有相关进程
pkill -f "yudao-module-cpa" 2>/dev/null || true
pkill -f "vite.*3000" 2>/dev/null || true

echo -e "${GREEN}[INFO]${NC} 所有服务已停止"
