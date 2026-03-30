@echo off
echo ========================================
echo  修复 SSL 证书问题并构建项目
echo ========================================
echo.

REM 设置正确的Java和Gradle路径
set JAVA_HOME=D:\Android\SDK\jdk-21
set GRADLE_HOME=D:\Android\gradle-9.3.1
set PATH=%JAVA_HOME%\bin;%GRADLE_HOME%\bin;%PATH%

echo 检查Java版本...
java -version
echo.

echo 检查Gradle版本...
gradle --version
echo.

echo "尝试方法1: 使用离线模式构建（如果依赖已缓存）"
echo --------------------------------------------------------
gradlew clean --offline
if %errorlevel% equ 0 (
    echo "离线清理成功，尝试构建..."
    gradlew assembleDebug --offline
    if %errorlevel% equ 0 (
        echo "构建成功！"
        goto success
    ) else (
        echo "离线构建失败，依赖可能未缓存"
    )
) else (
    echo "离线清理失败"
)
echo.

echo "尝试方法2: 临时禁用SSL验证"
echo --------------------------------------------------------
echo 创建临时Java安全设置...
(
echo // 临时禁用SSL验证的Java策略文件
echo grant {
echo     permission java.security.AllPermission;
echo };
) > "%TEMP%\temp.policy"

set JAVA_OPTS=-Djava.security.manager -Djava.security.policy=="%TEMP%\temp.policy" -Djavax.net.ssl.trustStoreType=JKS -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dhttps.protocols=TLSv1.2

echo 使用临时安全设置尝试构建...
set GRADLE_OPTS=%JAVA_OPTS%
gradlew clean
if %errorlevel% equ 0 (
    gradlew assembleDebug
    if %errorlevel% equ 0 (
        echo "构建成功！"
        goto success
    )
)

echo.

echo "尝试方法3: 直接使用本地Gradle（不通过包装器）"
echo --------------------------------------------------------
echo "注意：这需要项目配置支持..."
gradle clean
if %errorlevel% equ 0 (
    gradle assembleDebug
    if %errorlevel% equ 0 (
        echo "构建成功！"
        goto success
    )
)

echo.
echo ========================================
echo "所有方法都失败了。可能的原因："
echo "1. 缺少必要的依赖（需要网络连接）"
echo "2. SSL证书根证书问题"
echo "3. 防火墙或代理阻止连接"
echo ========================================
echo.
echo "建议解决方案："
echo "1. 检查网络连接"
echo "2. 尝试使用VPN"
echo "3. 手动下载缺少的依赖"
echo "4. 修复Java证书："
echo "   keytool -importcert -keystore \"%JAVA_HOME%\lib\security\cacerts\" -storepass changeit -alias gradle -file gradle_cert.pem"
exit /b 1

:success
echo.
echo ========================================
echo "构建完成！APK位置："
echo "D:\Android\BenDownAndroid\app\build\outputs\apk\debug\"
echo ========================================
exit /b 0