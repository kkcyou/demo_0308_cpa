# ====== 阶段1: Maven构建 ======
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先拷贝pom.xml下载依赖（利用Docker缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B \
    -DskipTests \
    --no-transfer-progress || true

# 拷贝源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ====== 阶段2: 运行环境 ======
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="CPA Daily"

WORKDIR /app

# 时区设置
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 从构建阶段拷贝jar
COPY --from=builder /build/target/yudao-module-cpa-1.0.0-SNAPSHOT.jar app.jar

# 存档目录
RUN mkdir -p /data/cpa-archive

EXPOSE 8080

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

ENTRYPOINT ["java", \
    "-Xms512m", "-Xmx1024m", \
    "-Dfile.encoding=UTF-8", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
