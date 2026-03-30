# 技术实现细节

本文档详细介绍了Markdown阅读器的技术架构和实现细节。

---

## 目录
1. [架构设计](#架构设计)
2. [核心模块](#核心模块)
3. [技术栈](#技术栈)
4. [代码示例](#代码示例)
5. [性能优化](#性能优化)
6. [依赖库](#依赖库)

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────┐
│                   UI层                          │
│  ┌──────────────┐  ┌──────────────────────┐  │
│  │ FileBrowser  │  │  MarkdownViewer      │  │
│  │   Screen     │  │      Screen          │  │
│  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│                业务逻辑层                        │
│  ┌──────────────────────────────────────────┐  │
│  │         MarkdownParser                   │  │
│  │    (解析、渲染、格式处理)                 │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│                  数据层                          │
│  ┌──────────────┐  ┌──────────────────────┐  │
│  │  FileUtils   │  │   AssetManager      │  │
│  │  (文件操作)   │  │   (资源管理)        │  │
│  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### 设计原则

1. **单一职责原则** - 每个类只负责一个功能
2. **开闭原则** - 对扩展开放，对修改关闭
3. **依赖倒置原则** - 依赖抽象而非具体实现
4. **KISS原则** - 保持简单，避免过度设计

---

## 核心模块

### 1. Markdown解析器

**职责：** 将Markdown文本解析为渲染组件

**核心功能：**
- 标题解析（#、##、###）
- 列表解析（有序、无序）
- 引用块解析
- 行内格式解析（粗体、斜体、代码）
- 代码块解析

### 2. 文件管理器

**职责：** 处理文件的读取、写入和管理

**核心功能：**
- 从Assets读取文件
- 文件列表获取
- 文件内容缓存
- 文件元数据解析

### 3. UI组件库

**职责：** 提供可复用的UI组件

**核心组件：**
- FileItem - 文件列表项
- MarkdownBlock - Markdown内容块
- CodeBlock - 代码块组件
- QuoteBlock - 引用块组件

---

## 技术栈

### 前端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| **Kotlin** | 2.x | 主要开发语言 |
| **Jetpack Compose** | 最新 | UI框架 |
| **Material 3** | 最新 | 设计系统 |
| **Coroutines** | 最新 | 异步处理 |
| **StateFlow** | 最新 | 状态管理 |

### 开发工具

| 工具 | 用途 |
|------|------|
| **Android Studio** | IDE |
| **Gradle** | 构建工具 |
| **Git** | 版本控制 |

---

## 代码示例

### 1. 主Activity实现

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MarkdownReaderApp()
            }
        }
    }
}
```

### 2. Markdown解析器核心逻辑

```kotlin
@Composable
fun MarkdownContent(content: String) {
    val lines = content.lines()
    var i = 0
    
    while (i < lines.size) {
        when {
            lines[i].startsWith("#") -> {
                // 处理标题
                renderHeading(lines[i])
                i++
            }
            lines[i].startsWith("- ") -> {
                // 处理列表
                renderList(lines, i)
                i += listSize
            }
            else -> {
                // 处理段落
                renderParagraph(lines[i])
                i++
            }
        }
    }
}
```

### 3. 行内格式解析

```kotlin
fun parseInlineFormatting(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // 粗体
                text.substring(i, i + 2) == "**" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(parseBoldContent(text, i))
                    }
                }
                // 斜体
                text[i] == '*' -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(parseItalicContent(text, i))
                    }
                }
                // 行内代码
                text[i] == '`' -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(parseCodeContent(text, i))
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
```

### 4. 文件读取实现

```kotlin
suspend fun readAssetFile(context: Context, fileName: String): String {
    return withContext(Dispatchers.IO) {
        context.assets.open(fileName).use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }
    }
}
```

### 5. 状态管理示例

```kotlin
@Composable
fun MarkdownReaderApp() {
    var selectedFile by remember { mutableStateOf<MarkdownFile?>(null) }
    
    if (selectedFile == null) {
        FileListScreen(
            onFileSelected = { file -> 
                selectedFile = file 
            }
        )
    } else {
        MarkdownViewerScreen(
            file = selectedFile!!,
            onBack = { 
                selectedFile = null 
            }
        )
    }
}
```

---

## 性能优化

### 1. 渲染优化

**问题：** 大文件渲染卡顿

**解决方案：**
```kotlin
// 使用 LazyColumn 替代 Column
LazyColumn {
    items(markdownBlocks) { block ->
        MarkdownBlock(block)
    }
}

// 只渲染可见区域
// 使用 remember 缓存已渲染的内容
```

### 2. 内存优化

**问题：** 图片和长文档内存占用高

**解决方案：**
```kotlin
// 图片压缩和缓存
// 大文件分页加载
// 及时释放不再使用的资源
```

### 3. 启动优化

**问题：** 应用启动时间过长

**解决方案：**
```kotlin
// 延迟初始化非核心组件
// 异步加载文件列表
// 简化启动页UI
```

---

## 依赖库

### 核心依赖

| 库名 | 版本 | 用途 | 来源 |
|------|------|------|------|
| **AndroidX Core KTX** | 1.10.x | 核心扩展 | Google |
| **Compose BOM** | 2024.x | Compose Bill of Materials | Google |
| **Material 3** | 最新 | Material Design组件 | Google |
| **Lifecycle Runtime** | 2.6.x | 生命周期管理 | Google |
| **Activity Compose** | 1.8.x | Activity与Compose集成 | Google |

### 测试依赖

| 库名 | 用途 |
|------|------|
| **JUnit** | 单元测试 |
| **Espresso** | UI测试 |
| **Compose Test** | Compose UI测试 |

---

## 最佳实践

### 代码规范

1. **命名规范**
   - 类名使用大驼峰：`MarkdownParser`
   - 函数名使用小驼峰：`parseContent()`
   - 常量使用全大写下划线：`MAX_FILE_SIZE`

2. **注释规范**
   - 公共API必须添加KDoc
   - 复杂逻辑必须添加注释
   -  TODO必须标注负责人和日期

3. **代码组织**
   - 相关功能放在同一个文件
   - 文件大小控制在500行以内
   - 合理使用扩展函数

### Git工作流

1. **分支策略**
   - `main` - 主分支，保持稳定
   - `feature/xxx` - 功能分支
   - `bugfix/xxx` - 修复分支

2. **提交规范**
   ```
   feat: 添加Markdown表格支持
   fix: 修复代码块渲染问题
   docs: 更新技术文档
   refactor: 重构解析器逻辑
   ```

---

## 常见问题

### Q: 如何添加新的Markdown语法支持？

**A:** 
1. 在`MarkdownContent`函数中添加新的when分支
2. 创建对应的渲染函数
3. 编写单元测试验证
4. 更新本文档

### Q: 大文件渲染慢怎么办？

**A:**
1. 实现分页加载
2. 使用LazyColumn懒加载
3. 添加渲染缓存
4. 考虑使用WebView渲染

### Q: 如何调试解析问题？

**A:**
1. 使用Log输出解析过程
2. 添加断点调试
3. 编写单元测试覆盖
4. 使用示例文档测试

---

## 参考资料

### 官方文档
- [Kotlin官方文档](https://kotlinlang.org/docs/)
- [Jetpack Compose官方文档](https://developer.android.com/jetpack/compose)
- [Material 3设计指南](https://m3.material.io/)

### Markdown规范
- [CommonMark规范](https://commonmark.org/)
- [GitHub Flavored Markdown](https://github.github.com/gfm/)

### 开源项目
- [Markwon](https://github.com/noties/Markwon) - Android Markdown库
- [Compose Markdown](https://github.com/jeziellago/compose-markdown) - Compose Markdown库

---

**文档结束**

*最后更新：2024年*

---

## 附录：更多代码示例

### 完整的文件数据模型

```kotlin
data class MarkdownFile(
    val id: Long = 0,
    val name: String,
    val path: String,
    val size: Long = 0,
    val lastModified: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val readCount: Int = 0
) {
    val extension: String
        get() = name.substringAfterLast('.', "")
    
    val displayName: String
        get() = name.substringBeforeLast('.')
    
    val readableSize: String
        get() = FileUtils.formatSize(size)
    
    val readableLastModified: String
        get() = DateUtils.formatTime(lastModified)
}
```

### 自定义主题配置

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
)

@Composable
fun MarkdownReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

**文档真正结束**