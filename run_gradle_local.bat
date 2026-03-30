@echo off
REM 设置本地 Gradle 路径
set GRADLE_HOME=D:\Android\gradle-9.3.1
set PATH=%GRADLE_HOME%\bin;%PATH%

REM 设置 Java 路径
set JAVA_HOME=D:\Android\SDK\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 运行 Gradle 构建
echo 使用本地 Gradle 版本: %GRADLE_HOME%
echo 使用 Java 版本: %JAVA_HOME%
echo.

REM 清理之前的构建
call gradlew clean --offline

REM 尝试同步项目
call gradlew build --offline

REM 如果失败，尝试不使用离线模式（但会因证书问题失败）
if errorlevel 1 (
    echo.
    echo "离线构建失败，尝试清理缓存..."
    call gradlew clean
    call gradlew build
)