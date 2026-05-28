#!/bin/bash

echo "正在从阿里云镜像下载Maven..."

# 使用阿里云镜像下载Maven
cd /tmp
curl -fsSL https://mirrors.aliyun.com/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o maven.tar.gz

if [ $? -ne 0 ]; then
    echo "阿里云镜像下载失败，尝试华为云镜像..."
    curl -fsSL https://repo.huaweicloud.com/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o maven.tar.gz
fi

if [ -f maven.tar.gz ]; then
    echo "下载成功，正在解压..."
    tar -xzf maven.tar.gz
    rm maven.tar.gz
    
    # 设置环境变量
    export MAVEN_HOME=/tmp/apache-maven-3.9.6
    export PATH=$MAVEN_HOME/bin:$PATH
    
    echo "Maven安装完成！"
    mvn -version
    
    echo ""
    echo "正在启动Spring Boot应用..."
    cd /Users/m1/Desktop/backstage-end
    
    # 启动应用
    mvn spring-boot:run
else
    echo "Maven下载失败，请手动安装Maven或使用IDE运行项目"
    exit 1
fi
