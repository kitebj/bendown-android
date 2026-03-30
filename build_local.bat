@echo off
echo 使用本地 Gradle 构建项目...
echo.

REM 设置环境变量
set JAVA_HOME=D:\Android\SDK\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 设置 Gradle 路径
set GRADLE_HOME=D:\Android\gradle-9.3.1
set PATH=%GRADLE_HOME%\bin;%PATH%

REM 检查 Gradle 是否可用
gradle --version

echo.
echo 开始构建项目...
echo.

REM 使用 Gradle 而不是 gradlew
gradle clean
gradle assembleDebug