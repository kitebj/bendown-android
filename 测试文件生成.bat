@echo off
echo 创建测试Markdown文件...
echo.

REM 创建测试目录（在项目内，方便测试）
if not exist "test_md_files" mkdir test_md_files

REM 创建测试文件1：简介
(
echo # 欢迎使用Markdown阅读器
echo.
echo 这是一个测试文件，用于验证Markdown阅读器的基本功能。
echo.
echo ## 功能列表
echo.
echo - 文件浏览
echo - Markdown解析
echo - 基础渲染
echo - 文件切换
echo.
echo ## 技术栈
echo.
echo - Kotlin
echo - Jetpack Compose
echo - CommonMark解析库
echo.
echo > 提示：这只是第一个测试文件
) > test_md_files\welcome.md

REM 创建测试文件2：开发计划
(
echo # 开发路线图
echo.
echo ## 版本规划
echo.
echo ### V0.1 - 基础阅读器
echo 1. 文件列表浏览
echo 2. Markdown基础解析
echo 3. 简单UI展示
echo.
echo ### V0.2 - 编辑功能
echo 1. 基础编辑
echo 2. 保存功能
echo 3. 撤销/重做
echo.
echo ### V0.3 - 增强体验
echo 1. 主题切换
echo 2. 字体调整
echo 3. 搜索功能
) > test_md_files\roadmap.md

REM 创建测试文件3：技术说明
(
echo # 技术实现细节
echo.
echo ## 架构设计
echo.
echo ```kotlin
echo // 示例代码
echo class MarkdownReaderApp : ComponentActivity() {
echo     override fun onCreate(savedInstanceState: Bundle?) {
echo         super.onCreate(savedInstanceState)
echo         setContent {
echo             MarkdownReaderTheme {
echo                 FileBrowserScreen()
echo             }
echo         }
echo     }
echo }
echo ```
echo.
echo ## 依赖库
echo.
echo | 库名 | 用途 | 版本 |
echo |------|------|------|
echo | CommonMark | Markdown解析 | 0.21.0 |
echo | Coil | 图片加载 | 2.4.0 |
echo | AndroidX | 基础框架 | 最新 |
echo.
echo ## 注意事项
echo.
echo 1. 文件权限需要申请
echo 2. 大文件需要分块加载
echo 3. 内存管理需要注意
) > test_md_files\tech_details.md

echo 测试文件创建完成：
dir test_md_files\

echo.
echo 文件位置：D:\Android\BenDownAndroid\test_md_files\
echo.
echo 下一步：将这些文件复制到Android设备的/sdcard/MarkdownTest/目录下
pause