#!/bin/bash
#=============================================
# CPA Daily - 阿里云一键部署脚本
# 在本地 Windows Git Bash / WSL 中运行
# 功能: 打包代码 → 上传到服务器 → SSH执行部署
#
# 使用方式:
#   chmod +x scripts/cloud-deploy.sh
#   ./scripts/cloud-deploy.sh <服务器IP> [SSH用户名] [SSH端口]
#
# 示例:
#   ./scripts/cloud-deploy.sh 47.100.1.2
#   ./scripts/cloud-deploy.sh 47.100.1.2 root 22
#=============================================

set -e

# === 参数解析 ===
SERVER_IP=${1:?"用法: $0 <服务器IP> [用户名] [端口]"}
SSH_USER=${2:-root}
SSH_PORT=${3:-22}
REMOTE_DIR="/opt/cpa-daily"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "============================================="
echo "  CPA Daily 阿里云部署"
echo "  服务器: ${SSH_USER}@${SERVER_IP}:${SSH_PORT}"
echo "  远程目录: ${REMOTE_DIR}"
echo "============================================="
echo ""

# === 第1步: 本地打包 ===
echo "[1/4] 打包项目文件..."
cd "$PROJECT_DIR"

# 创建临时打包目录（排除不需要的文件）
TAR_FILE="/tmp/cpa-daily-deploy.tar.gz"
tar czf "$TAR_FILE" \
    --exclude='target' \
    --exclude='node_modules' \
    --exclude='frontend/node_modules' \
    --exclude='frontend/dist' \
    --exclude='.git' \
    --exclude='*.class' \
    --exclude='*.jar' \
    --exclude='scripts/deploy.sh' \
    --exclude='scripts/stop.sh' \
    --exclude='scripts/restart.sh' \
    -C "$(dirname "$PROJECT_DIR")" \
    "$(basename "$PROJECT_DIR")"

FILE_SIZE=$(du -h "$TAR_FILE" | cut -f1)
echo "    打包完成: ${TAR_FILE} (${FILE_SIZE})"

# === 第2步: 上传到服务器 ===
echo ""
echo "[2/4] 上传到服务器..."
ssh -p "$SSH_PORT" "${SSH_USER}@${SERVER_IP}" "mkdir -p ${REMOTE_DIR}"
scp -P "$SSH_PORT" "$TAR_FILE" "${SSH_USER}@${SERVER_IP}:${REMOTE_DIR}/cpa-daily-deploy.tar.gz"
echo "    上传完成"

# === 第3步: 服务器端解压 ===
echo ""
echo "[3/4] 服务器端解压和准备..."
ssh -p "$SSH_PORT" "${SSH_USER}@${SERVER_IP}" << 'REMOTE_SCRIPT'
set -e
REMOTE_DIR="/opt/cpa-daily"
cd "$REMOTE_DIR"

# 解压（覆盖旧文件）
tar xzf cpa-daily-deploy.tar.gz --strip-components=1
rm -f cpa-daily-deploy.tar.gz

# 如果.env不存在，从模板复制
if [ ! -f docker/.env ]; then
    cp docker/.env.example docker/.env
    echo "    [注意] 已创建 docker/.env，请编辑填入AI API Key:"
    echo "    vim ${REMOTE_DIR}/docker/.env"
fi

echo "    解压完成"
REMOTE_SCRIPT

# === 第4步: 构建并启动 ===
echo ""
echo "[4/4] 构建Docker镜像并启动服务..."
ssh -p "$SSH_PORT" "${SSH_USER}@${SERVER_IP}" << 'REMOTE_SCRIPT'
set -e
REMOTE_DIR="/opt/cpa-daily"
cd "$REMOTE_DIR/docker"

# 停止旧容器（如果有）
docker compose down 2>/dev/null || true

# 构建并启动
docker compose up -d --build

echo ""
echo "============================================="
echo "  部署完成！"
echo "============================================="
echo ""
echo "  前端地址: http://$(hostname -I | awk '{print $1}'):80"
echo "  后端API:  http://$(hostname -I | awk '{print $1}'):8080"
echo "  Swagger:  http://$(hostname -I | awk '{print $1}'):80/swagger-ui/index.html"
echo ""
echo "  查看日志:"
echo "    docker compose logs -f backend"
echo "    docker compose logs -f frontend"
echo ""
echo "  重要: 如果还没配置AI API Key，请执行:"
echo "    vim ${REMOTE_DIR}/docker/.env"
echo "    docker compose restart backend"
echo ""
REMOTE_SCRIPT

# 清理本地临时文件
rm -f "$TAR_FILE"

echo ""
echo "本地部署脚本执行完毕！"
