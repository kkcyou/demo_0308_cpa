#!/bin/bash
#=============================================
# CPA Daily - Docker Compose 部署方案（可选）
# 如果你的WSL已安装Docker，可以用这个方案
# 使用方式: ./docker-deploy.sh
#=============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 生成 docker-compose.yml
cat > "$PROJECT_DIR/docker-compose.yml" << 'EOF'
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: cpa-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: yudao_cpa
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - ./sql/cpa_schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - mysql_data:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    container_name: cpa-redis
    ports:
      - "6379:6379"

volumes:
  mysql_data:
EOF

echo "[INFO] docker-compose.yml 已生成"
echo "[INFO] 启动 MySQL 和 Redis..."

cd "$PROJECT_DIR"
docker compose up -d

echo "[INFO] 等待MySQL初始化（约15秒）..."
sleep 15

echo "[INFO] 基础服务已就绪，现在可以运行以下命令启动应用："
echo ""
echo "  # 构建并启动后端"
echo "  cd $PROJECT_DIR"
echo "  mvn clean package -DskipTests"
echo "  java -jar target/yudao-module-cpa-1.0.0-SNAPSHOT.jar"
echo ""
echo "  # 另开终端启动前端"
echo "  cd $PROJECT_DIR/frontend"
echo "  npm install && npx vite --host 0.0.0.0"
