#!/bin/bash
#=============================================
# CPA Daily - WSL 一键部署脚本
# 使用方式: chmod +x deploy.sh && ./deploy.sh
#=============================================

set -e

# ====== 颜色定义 ======
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "\n${BLUE}========== $1 ==========${NC}"; }

# ====== 项目路径（WSL下Windows路径映射） ======
# 自动检测：如果从WSL访问Windows路径
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

log_info "项目目录: $PROJECT_DIR"

# ====== 配置变量 ======
MYSQL_ROOT_PASSWORD="root"
MYSQL_DATABASE="yudao_cpa"
REDIS_PORT=6379
BACKEND_PORT=8080
FRONTEND_PORT=3000

# AI API Key（部署前请修改）
export AI_OPENAI_API_KEY="${AI_OPENAI_API_KEY:-sk-xxx}"
export AI_CLAUDE_API_KEY="${AI_CLAUDE_API_KEY:-}"

#=============================================
# 步骤1: 检查并安装依赖
#=============================================
install_dependencies() {
    log_step "步骤1: 检查并安装系统依赖"

    # 更新包列表
    sudo apt-get update -qq

    # --- Java 17 ---
    if java -version 2>&1 | grep -q "17"; then
        log_info "Java 17 已安装"
    else
        log_info "安装 Java 17 (OpenJDK)..."
        sudo apt-get install -y openjdk-17-jdk
    fi
    java -version 2>&1 | head -1

    # --- Maven ---
    if command -v mvn &> /dev/null; then
        log_info "Maven 已安装"
    else
        log_info "安装 Maven..."
        sudo apt-get install -y maven
    fi
    mvn -v 2>&1 | head -1

    # --- MySQL ---
    if command -v mysql &> /dev/null; then
        log_info "MySQL 已安装"
    else
        log_info "安装 MySQL 8.0..."
        sudo apt-get install -y mysql-server
    fi

    # --- Redis ---
    if command -v redis-server &> /dev/null; then
        log_info "Redis 已安装"
    else
        log_info "安装 Redis..."
        sudo apt-get install -y redis-server
    fi

    # --- Node.js ---
    if command -v node &> /dev/null && [[ $(node -v | cut -d. -f1 | tr -d 'v') -ge 18 ]]; then
        log_info "Node.js $(node -v) 已安装"
    else
        log_info "安装 Node.js 20.x..."
        curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
        sudo apt-get install -y nodejs
    fi
    node -v
    npm -v

    log_info "所有依赖安装完成"
}

#=============================================
# 步骤2: 启动MySQL和Redis服务
#=============================================
start_services() {
    log_step "步骤2: 启动MySQL和Redis服务"

    # 启动MySQL
    if sudo service mysql status 2>&1 | grep -q "running"; then
        log_info "MySQL 已在运行"
    else
        log_info "启动 MySQL..."
        sudo service mysql start
    fi

    # 启动Redis
    if sudo service redis-server status 2>&1 | grep -q "running"; then
        log_info "Redis 已在运行"
    else
        log_info "启动 Redis..."
        sudo service redis-server start
    fi

    # 验证连接
    log_info "验证MySQL连接..."
    if sudo mysql -u root -e "SELECT 1" &>/dev/null; then
        log_info "MySQL连接成功（无密码模式）"
        MYSQL_CMD="sudo mysql -u root"
    elif mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "SELECT 1" &>/dev/null; then
        log_info "MySQL连接成功（密码模式）"
        MYSQL_CMD="mysql -u root -p${MYSQL_ROOT_PASSWORD}"
    else
        log_warn "MySQL连接失败，尝试重置root密码..."
        sudo mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}'; FLUSH PRIVILEGES;" 2>/dev/null || true
        MYSQL_CMD="mysql -u root -p${MYSQL_ROOT_PASSWORD}"
    fi

    log_info "验证Redis连接..."
    redis-cli ping && log_info "Redis连接成功"
}

#=============================================
# 步骤3: 初始化数据库
#=============================================
init_database() {
    log_step "步骤3: 初始化数据库"

    SQL_FILE="$PROJECT_DIR/sql/cpa_schema.sql"
    if [ ! -f "$SQL_FILE" ]; then
        log_error "SQL文件不存在: $SQL_FILE"
        exit 1
    fi

    log_info "执行建表脚本..."
    $MYSQL_CMD < "$SQL_FILE"

    # 验证表是否创建成功
    TABLE_COUNT=$($MYSQL_CMD -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${MYSQL_DATABASE}'")
    log_info "数据库 ${MYSQL_DATABASE} 中共有 ${TABLE_COUNT} 张表"

    if [ "$TABLE_COUNT" -lt 10 ]; then
        log_error "表数量不足，期望10张，实际${TABLE_COUNT}张"
        exit 1
    fi

    log_info "数据库初始化完成"
}

#=============================================
# 步骤4: 插入测试数据
#=============================================
insert_test_data() {
    log_step "步骤4: 插入考点和真题测试数据"

    $MYSQL_CMD "${MYSQL_DATABASE}" << 'EOSQL'

-- 检查是否已有数据
SET @count = (SELECT COUNT(*) FROM cpa_knowledge_point);
-- 只在空表时插入

INSERT IGNORE INTO cpa_knowledge_point (id, subject, chapter, title, content, importance, frequency, keywords, parent_id, sort, status) VALUES
-- 会计（subject=1）
(1, 1, '第二章 存货', '存货的初始计量', '存货应当按照成本进行初始计量。存货成本包括采购成本、加工成本和其他成本。', 4, 4, '存货,初始计量,采购成本,加工成本', 0, 1, 0),
(2, 1, '第五章 长期股权投资', '长期股权投资-权益法', '投资方对被投资单位具有重大影响的长期股权投资，应当采用权益法核算。', 5, 5, '长期股权投资,权益法,重大影响', 0, 2, 0),
(3, 1, '第五章 长期股权投资', '长期股权投资-成本法', '投资方能够对被投资单位实施控制的长期股权投资，应当采用成本法核算。', 5, 5, '长期股权投资,成本法,控制', 0, 3, 0),
(4, 1, '第八章 金融工具', '金融资产分类', '金融资产根据业务模式和合同现金流量特征分为三类：AC、FVOCI、FVTPL。', 5, 5, '金融资产,分类,AC,FVOCI,FVTPL', 0, 4, 0),
(5, 1, '第十四章 收入', '收入确认五步法', '识别合同→识别履约义务→确定交易价格→分摊交易价格→确认收入。', 5, 5, '收入确认,五步法,履约义务', 0, 5, 0),
-- 审计（subject=2）
(6, 2, '第四章 审计抽样', '属性抽样', '用于控制测试，目的是估计总体中某一事件发生率的统计抽样方法。', 4, 4, '审计抽样,属性抽样,控制测试', 0, 1, 0),
(7, 2, '第八章 审计报告', '审计报告类型', '标准无保留意见、带强调事项段的无保留意见、保留意见、否定意见、无法表示意见。', 5, 5, '审计报告,意见类型,保留意见', 0, 2, 0),
(8, 2, '第五章 函证', '函证程序', '函证是审计人员直接从第三方获取书面答复的审计程序。', 4, 4, '函证,银行函证,应收账款函证', 0, 3, 0),
-- 财管（subject=3）
(9, 3, '第二章 财务比率分析', '杜邦分析体系', 'ROE=销售净利率×资产周转率×权益乘数，将盈利能力、运营效率和财务杠杆联系起来。', 5, 5, '杜邦分析,ROE,权益乘数', 0, 1, 0),
(10, 3, '第六章 资本预算', 'NPV净现值法', '将未来现金流按资本成本折现后减去初始投资，NPV>0则项目可行。', 5, 5, 'NPV,净现值,资本预算,折现', 0, 2, 0),
-- 经济法（subject=4）
(11, 4, '第二章 公司法', '股东出资制度', '股东可以用货币出资，也可以用实物、知识产权、土地使用权等非货币财产作价出资。', 4, 4, '股东出资,货币出资,非货币出资', 0, 1, 0),
(12, 4, '第五章 合同法', '合同的效力', '合同的成立与生效、无效合同、可撤销合同、效力待定合同。', 5, 5, '合同效力,无效合同,可撤销合同', 0, 2, 0),
-- 税法（subject=5）
(13, 5, '第二章 增值税', '增值税税率', '现行税率：13%、9%、6%、0%。一般纳税人与小规模纳税人的区别。', 5, 5, '增值税,税率,一般纳税人,小规模', 0, 1, 0),
(14, 5, '第四章 企业所得税', '企业所得税税前扣除', '工资薪金、职工福利费、工会经费、职工教育经费的扣除标准。', 5, 5, '企业所得税,税前扣除,福利费', 0, 2, 0),
-- 战略（subject=6）
(15, 6, '第二章 战略分析', 'SWOT分析', '分析企业的优势(S)、劣势(W)、机会(O)、威胁(T)，制定战略。', 4, 4, 'SWOT,战略分析,优势,劣势', 0, 1, 0),
(16, 6, '第四章 公司治理', '公司治理结构', '股东大会、董事会、监事会的职权划分和制衡关系。', 4, 4, '公司治理,董事会,监事会,股东大会', 0, 2, 0);

-- 插入真题测试数据
INSERT IGNORE INTO cpa_exam_question (id, subject, year, question_type, content, options, answer, analysis, knowledge_point_ids, difficulty) VALUES
(1, 1, 2023, 1,
 '甲公司对乙公司的长期股权投资采用权益法核算。2023年度，乙公司实现净利润1000万元。甲公司持有乙公司30%的股权。不考虑其他因素，甲公司应确认的投资收益为：',
 '{"A":"100万元","B":"200万元","C":"300万元","D":"400万元"}',
 'C',
 '权益法下，投资方按持股比例确认投资收益。投资收益=1000×30%=300万元。',
 '2', 3),
(2, 5, 2023, 1,
 '下列关于增值税税率的表述中，正确的是：',
 '{"A":"纳税人销售交通运输服务，税率为6%","B":"纳税人销售不动产，税率为9%","C":"纳税人出口货物，税率为13%","D":"小规模纳税人适用3%的税率"}',
 'B',
 '交通运输服务税率9%（非6%）；出口货物零税率（非13%）；小规模纳税人适用征收率（非税率）。',
 '13', 2),
(3, 1, 2022, 1,
 '企业确认收入应当遵循的五步法模型中，第一步是：',
 '{"A":"识别履约义务","B":"识别与客户订立的合同","C":"确定交易价格","D":"将交易价格分摊至各单项履约义务"}',
 'B',
 '收入确认五步法：①识别合同→②识别履约义务→③确定交易价格→④分摊交易价格→⑤确认收入。第一步是识别与客户订立的合同。',
 '5', 2);

EOSQL

    POINT_COUNT=$($MYSQL_CMD -N -e "SELECT COUNT(*) FROM ${MYSQL_DATABASE}.cpa_knowledge_point")
    QUESTION_COUNT=$($MYSQL_CMD -N -e "SELECT COUNT(*) FROM ${MYSQL_DATABASE}.cpa_exam_question")
    log_info "测试数据: ${POINT_COUNT} 个考点, ${QUESTION_COUNT} 道真题"
}

#=============================================
# 步骤5: 构建后端
#=============================================
build_backend() {
    log_step "步骤5: 构建后端 Spring Boot 应用"

    cd "$PROJECT_DIR"

    log_info "执行 mvn clean package（跳过测试）..."
    mvn clean package -DskipTests -q

    if [ -f "target/yudao-module-cpa-1.0.0-SNAPSHOT.jar" ]; then
        log_info "后端构建成功: target/yudao-module-cpa-1.0.0-SNAPSHOT.jar"
    else
        log_error "后端构建失败"
        exit 1
    fi
}

#=============================================
# 步骤6: 安装前端依赖
#=============================================
build_frontend() {
    log_step "步骤6: 安装前端依赖"

    cd "$PROJECT_DIR/frontend"

    log_info "执行 npm install..."
    npm install --legacy-peer-deps

    log_info "前端依赖安装完成"
}

#=============================================
# 步骤7: 启动应用
#=============================================
start_app() {
    log_step "步骤7: 启动应用"

    cd "$PROJECT_DIR"

    # 杀掉旧进程
    log_info "清理旧进程..."
    pkill -f "yudao-module-cpa" 2>/dev/null || true
    sleep 1

    # 启动后端
    log_info "启动后端 (端口: ${BACKEND_PORT})..."
    nohup java -jar target/yudao-module-cpa-1.0.0-SNAPSHOT.jar \
        --spring.datasource.password="${MYSQL_ROOT_PASSWORD}" \
        > logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo "$BACKEND_PID" > logs/backend.pid
    log_info "后端PID: $BACKEND_PID"

    # 等待后端启动
    log_info "等待后端启动..."
    for i in $(seq 1 30); do
        if curl -s http://localhost:${BACKEND_PORT}/v3/api-docs > /dev/null 2>&1; then
            log_info "后端启动成功！"
            break
        fi
        if [ $i -eq 30 ]; then
            log_error "后端启动超时，查看日志: tail -f logs/backend.log"
            exit 1
        fi
        sleep 2
    done

    # 启动前端
    log_info "启动前端开发服务器 (端口: ${FRONTEND_PORT})..."
    cd "$PROJECT_DIR/frontend"
    nohup npx vite --host 0.0.0.0 > "$PROJECT_DIR/logs/frontend.log" 2>&1 &
    FRONTEND_PID=$!
    echo "$FRONTEND_PID" > "$PROJECT_DIR/logs/frontend.pid"
    log_info "前端PID: $FRONTEND_PID"

    sleep 3
}

#=============================================
# 步骤8: 输出访问信息
#=============================================
print_info() {
    log_step "部署完成！"

    # 获取WSL IP
    WSL_IP=$(hostname -I | awk '{print $1}')

    echo -e "
${GREEN}╔══════════════════════════════════════════════════╗
║           CPA Daily 部署成功！                    ║
╠══════════════════════════════════════════════════╣
║                                                  ║
║  前端页面:  http://localhost:${FRONTEND_PORT}              ║
║  后端API:   http://localhost:${BACKEND_PORT}              ║
║  Swagger:   http://localhost:${BACKEND_PORT}/swagger-ui.html ║
║                                                  ║
║  WSL IP:    ${WSL_IP}                      ║
║  Windows访问: http://${WSL_IP}:${FRONTEND_PORT}          ║
║                                                  ║
╠══════════════════════════════════════════════════╣
║  管理命令:                                        ║
║    查看后端日志:  tail -f logs/backend.log         ║
║    查看前端日志:  tail -f logs/frontend.log        ║
║    停止应用:      ./scripts/stop.sh               ║
║    重启应用:      ./scripts/restart.sh             ║
║                                                  ║
╠══════════════════════════════════════════════════╣
║  测试API:                                         ║
║    手动生成今日内容:                               ║
║    curl -X POST http://localhost:8080/app-api/cpa/daily/generate ║
║                                                  ║
║    查看考点列表:                                   ║
║    curl http://localhost:8080/admin-api/cpa/knowledge-point/list-all ║
║                                                  ║
╚══════════════════════════════════════════════════╝${NC}
"

    log_warn "注意: AI功能需要配置API Key，请修改环境变量:"
    echo "  export AI_OPENAI_API_KEY=your-key"
    echo "  export AI_CLAUDE_API_KEY=your-key"
    echo ""
}

#=============================================
# 主流程
#=============================================
main() {
    echo -e "${BLUE}"
    echo "  ╔═══════════════════════════════════════╗"
    echo "  ║   CPA Daily - 注会每日学习助手        ║"
    echo "  ║   WSL 一键部署脚本 v1.0               ║"
    echo "  ╚═══════════════════════════════════════╝"
    echo -e "${NC}"

    # 创建日志目录
    mkdir -p "$PROJECT_DIR/logs"

    install_dependencies
    start_services
    init_database
    insert_test_data
    build_backend
    build_frontend
    start_app
    print_info
}

main "$@"
