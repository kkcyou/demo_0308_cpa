#!/bin/bash
#=============================================
# CPA Daily - 重启应用脚本
#=============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "停止现有服务..."
"$SCRIPT_DIR/stop.sh"

sleep 2

echo "重新启动..."
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 确保MySQL和Redis运行
sudo service mysql start 2>/dev/null || true
sudo service redis-server start 2>/dev/null || true

mkdir -p "$PROJECT_DIR/logs"

# 启动后端
cd "$PROJECT_DIR"
nohup java -jar target/yudao-module-cpa-1.0.0-SNAPSHOT.jar \
    > logs/backend.log 2>&1 &
echo $! > logs/backend.pid
echo "[INFO] 后端已启动 (PID: $(cat logs/backend.pid))"

# 等待后端
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/v3/api-docs > /dev/null 2>&1; then
        echo "[INFO] 后端就绪"
        break
    fi
    sleep 2
done

# 启动前端
cd "$PROJECT_DIR/frontend"
nohup npx vite --host 0.0.0.0 > "$PROJECT_DIR/logs/frontend.log" 2>&1 &
echo $! > "$PROJECT_DIR/logs/frontend.pid"
echo "[INFO] 前端已启动 (PID: $(cat "$PROJECT_DIR/logs/frontend.pid"))"

echo ""
echo "[INFO] 重启完成！"
echo "  前端: http://localhost:3000"
echo "  后端: http://localhost:8080"
echo "  Swagger: http://localhost:8080/swagger-ui.html"
