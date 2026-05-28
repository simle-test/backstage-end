#!/bin/bash

echo "正在安装Maven..."

# 下载Maven
cd /tmp
curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o maven.tar.gz

if [ $? -ne 0 ]; then
    echo "下载失败，尝试使用备用源..."
    curl -fsSL https://mirrors.aliyun.com/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o maven.tar.gz
fi

# 解压
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
