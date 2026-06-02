# 多阶段构建：先构建前端，再打包后端
# Stage 1: 前端构建
FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY ai-job-market-frontend/package*.json ./
RUN npm ci
COPY ai-job-market-frontend/ ./
RUN npm run build

# Stage 2: 后端构建
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src/ ./src/
RUN mvn package -DskipTests -B

# Stage 3: 运行镜像
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 复制后端 jar
COPY --from=backend-build /app/target/*.jar app.jar
# 复制前端构建产物到 static 目录（Spring Boot 自动提供静态文件）
COPY --from=frontend-build /frontend/dist/ /app/static/

EXPOSE 8123
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
