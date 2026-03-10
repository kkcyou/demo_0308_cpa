#!/bin/bash
#=============================================
# CPA Daily - 服务器端管理脚本
# 放在服务器 /opt/cpa-daily/ 下使用
#
# 用法:
#   ./scripts/server-manage.sh start    # 启动所有服务
#   ./scripts/server-manage.sh stop     # 停止所有服务
#   ./scripts/server-manage.sh restart  # 重启所有服务
#   ./scripts/server-manage.sh status   # 查看状态
#   ./scripts/server-manage.sh logs     # 查看后端日志
#   ./scripts/server-manage.sh rebuild  # 重新构建并启动
#=============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_DIR="$PROJECT_DIR/docker"

cd "$COMPOSE_DIR"

case "${1:-status}" in
    start)
        echo "启动所有服务..."
        docker compose up -d
        docker compose ps
        ;;
    stop)
        echo "停止所有服务..."
        docker compose down
        ;;
    restart)
        echo "重启所有服务..."
        docker compose restart
        docker compose ps
        ;;
    status)
        docker compose ps
        ;;
    logs)
        docker compose logs -f --tail=100 ${2:-backend}
        ;;
    rebuild)
        echo "重新构建并启动..."
        docker compose down
        docker compose up -d --build
        docker compose ps
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status|logs|rebuild}"
        echo ""
        echo "  start    - 启动所有服务"
        echo "  stop     - 停止所有服务"
        echo "  restart  - 重启所有服务"
        echo "  status   - 查看容器状态"
        echo "  logs     - 查看日志 (默认backend，可指定: logs frontend)"
        echo "  rebuild  - 重新构建镜像并启动"
        exit 1
        ;;
esac
