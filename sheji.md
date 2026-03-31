针对你设想的 Android Markdown 阅读器（支持本地打开、外部唤起、分享接收），文件系统的设计建议如下：

## 一、核心原则

1. **不申请全局存储权限**（`READ_EXTERNAL_STORAGE`），使用现代 Android 存储访问框架（SAF）
2. **支持多种 URI 类型**：`content://`、`file://`（低版本兼容）、`media://`
3. **统一文件访问抽象层**，屏蔽不同 URI 的差异

## 二、文件访问架构

```
┌─────────────────────────────────────┐
│         UI 层（打开/分享/接收）       │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│      DocumentResolver（核心抽象）     │
│  - openInputStream(uri)              │
│  - getDisplayName(uri)               │
│  - getSize(uri)                      │
│  - getMimeType(uri)                  │
└─────────────────┬───────────────────┘
                  │
      ┌───────────┼───────────┐
      ▼           ▼           ▼
  ContentUri   FileUri    MediaUri
   Handler     Handler     Handler
```

## 三、关键实现点

### 1. 处理三种启动场景的 Intent Filter

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- 从文件管理器打开 -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/markdown" />
        <data android:mimeType="text/x-markdown" />
    </intent-filter>
    
    <!-- 接收分享 -->
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
        <data android:mimeType="text/markdown" />
    </intent-filter>
</activity>
```

### 2. 统一文件处理入口

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        when {
            intent.action == Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    openMarkdownFile(uri)
                }
            }
            intent.action == Intent.ACTION_SEND -> {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    openMarkdownFile(uri)
                } ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                    // 处理纯文本分享（如果是 Markdown 内容）
                    openMarkdownText(text)
                }
            }
            else -> {
                // 正常启动，显示文件选择器
                showFilePicker()
            }
        }
    }
}
```

### 3. DocumentResolver 核心实现

```kotlin
interface DocumentResolver {
    fun openInputStream(uri: Uri): InputStream?
    fun getDisplayName(uri: Uri): String?
    fun getSize(uri: Uri): Long
    fun getMimeType(uri: Uri): String?
}

class AndroidDocumentResolver(private val context: Context) : DocumentResolver {
    
    override fun openInputStream(uri: Uri): InputStream? {
        return try {
            when (uri.scheme) {
                "content" -> context.contentResolver.openInputStream(uri)
                "file" -> File(uri.path).inputStream()
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    override fun getDisplayName(uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex)
                else null
            }
        } else {
            uri.path?.substringAfterLast('/')
        }
    }
    
    override fun getSize(uri: Uri): Long {
        return if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst() && sizeIndex >= 0) cursor.getLong(sizeIndex)
                else -1
            } ?: -1
        } else {
            uri.path?.let { File(it).length() } ?: -1
        }
    }
    
    override fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.getType(uri)
        } else {
            // 根据文件扩展名判断
            val extension = uri.path?.substringAfterLast('.', "")
            when (extension?.lowercase()) {
                "md", "markdown" -> "text/markdown"
                else -> "text/plain"
            }
        }
    }
}
```

### 4. 文件选择器（不申请存储权限）

```kotlin
private fun showFilePicker() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/markdown"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/markdown", "text/x-markdown", "text/plain"))
    }
    startActivityForResult(intent, FILE_PICKER_REQUEST)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK) {
        data?.data?.let { uri ->
            // 持久化权限（可选，用于下次快速打开）
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            openMarkdownFile(uri)
        }
    }
}
```

## 四、高级功能建议

### 1. 最近文件列表
- 使用 `DataStore` 或 `Room` 存储最近打开的 URI
- 保存时同时存储 `uri.toString()`、`displayName`、`lastAccessTime`
- 注意：URI 可能失效（文件被删除或移动），需要捕获异常并清理

### 2. 图片资源处理
Markdown 中的本地图片引用需要特殊处理：
```kotlin
// 相对路径图片 -> 尝试基于原始文件目录解析
fun resolveImageUri(baseUri: Uri, imagePath: String): Uri? {
    return if (imagePath.startsWith("http")) {
        // 网络图片直接使用
        Uri.parse(imagePath)
    } else {
        // 相对路径，尝试在相同目录下查找
        val baseDir = baseUri.path?.substringBeforeLast('/')
        val absolutePath = "$baseDir/$imagePath"
        File(absolutePath).takeIf { it.exists() }?.toUri()
    }
}
```

### 3. 缓存策略
- 解析后的 Markdown 内容缓存到内存（LRU 缓存）
- 图片缓存到磁盘（使用 Glide/Coil 等库）
- 大文件（>1MB）分页加载

### 4. 错误处理
- **权限不足**：提示用户重新授权（通过 SAF 再次选择）
- **文件不存在**：从最近列表中移除并提示
- **格式错误**：降级为纯文本显示

## 五、技术选型建议

| 功能 | 推荐方案 | 原因 |
|------|---------|------|
| Markdown 解析 | [Markwon](https://github.com/noties/Markwon) | Android 原生、高可定制、性能好 |
| 图片加载 | [Coil](https://coil-kt.github.io/coil/) | 轻量、协程支持、OK |
| 文件选择 | 系统 `ACTION_OPEN_DOCUMENT` | 无需权限、系统维护 |
| URI 处理 | `DocumentFile` + `ContentResolver` | 兼容性好 |
| 文本编辑（可选） | 可扩展但阅读器不需要 | - |

## 六、安全注意事项

1. **验证传入的 URI**：防止恶意文件路径遍历
   ```kotlin
   uri.path?.let { path ->
       if (path.contains("../") || path.contains("..\\")) {
           throw SecurityException("Invalid path")
       }
   }
   ```

2. **限制文件大小**：解析前检查文件大小，避免 OOM
   ```kotlin
   const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
   ```

3. **隔离 WebView**：如果使用 WebView 渲染，禁用 JavaScript 文件访问

## 总结

核心思路是**不依赖全局存储权限**，通过 SAF + ContentResolver 统一处理各种 URI，为三种启动场景提供一致的访问接口。这样既符合 Android 现代存储规范（Scoped Storage），又能保证良好的用户体验和系统兼容性。