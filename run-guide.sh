#!/bin/bash

echo "由于Lombok与Java 26兼容性问题，建议使用IDE运行项目"
echo ""
echo "使用方法："
echo "1. 在IDE中打开 Application.java"
echo "2. 点击运行按钮启动应用"
echo ""
echo "或者，您可以降级Java版本到21："
echo "brew install openjdk@21"
echo "export JAVA_HOME=/usr/local/opt/openjdk@21"
echo ""
echo "然后重新运行："
echo "mvn spring-boot:run"
