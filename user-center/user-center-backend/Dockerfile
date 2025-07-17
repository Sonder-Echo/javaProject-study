# Docker 镜像构建
# 依赖的镜像
FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
# 镜像工作目录
WORKDIR /app
# 拷贝 pom.xml 和 src 文件夹
COPY pom.xml .
COPY src ./src

# Build a release artifact.
# 执行maven 打包
RUN mvn package -DskipTests

# Run the web service on container startup.
# 镜像启动命令
CMD ["java","-jar","/app/target/user-center-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]