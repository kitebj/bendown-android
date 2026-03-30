@echo off
echo ========================================
echo  检查Android Studio配置
echo ========================================
echo.

REM 检查关键文件是否存在
echo 1. 检查项目配置文件：
if exist "gradle.properties" (
    echo   ✓ gradle.properties 存在
) else (
    echo   ✗ gradle.properties 不存在
)

if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo   ✓ gradle-wrapper.properties 存在
) else (
    echo   ✗ gradle-wrapper.properties 不存在
)

echo.
echo 2. 检查Java和Gradle路径：
if exist "D:\Android\SDK\jdk-21" (
    echo   ✓ Java JDK 存在: D:\Android\SDK\jdk-21
) else (
    echo   ✗ Java JDK 不存在
)

if exist "D:\Android\gradle-9.3.1" (
    echo   ✓ Gradle 存在: D:\Android\gradle-9.3.1
) else (
    echo   ✗ Gradle 不存在
)

echo.
echo 3. 当前gradle.properties配置：
echo.
type gradle.properties | findstr /v "^#" | findstr /v "^$"

echo.
echo 4. gradle-wrapper.properties配置：
echo.
type gradle\wrapper\gradle-wrapper.properties | findstr /v "^#" | findstr /v "^$"

echo.
echo ========================================
echo "Android Studio 使用指南："
echo.
echo "A. 如果Android Studio无法构建："
echo "   1. 运行这个脚本启动Android Studio："
echo "      D:\Android\启动AndroidStudio.bat"
echo "   2. 在Android Studio中："
echo "      File → Invalidate Caches and Restart..."
echo "      Build → Clean Project"
echo "      Build → Rebuild Project"
echo.
echo "B. 如果仍有SSL问题："
echo "   1. 在Android Studio中："
echo "      File → Settings → Build, Execution, Deployment"
echo "      → Build Tools → Gradle"
echo "   2. 在'Gradle VM options'中添加："
echo "      -Djavax.net.ssl.trustStoreType=JKS"
echo "      -Djavax.net.ssl.trustStore="
echo "      -Djavax.net.ssl.trustStorePassword="
echo "      -Dcom.sun.net.ssl.checkRevocation=false"
echo "      -Dhttps.protocols=TLSv1.2"
echo.
echo "C. 备用方案："
echo "   使用右侧Gradle面板构建："
echo "   1. 点击右侧Gradle图标"
echo "   2. 展开项目 → app → Tasks → build"
echo "   3. 双击 assembleDebug"
echo ========================================
echo.

pause