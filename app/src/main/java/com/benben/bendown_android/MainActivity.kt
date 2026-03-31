package com.benben.bendown_android

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.benben.bendown_android.data.ClipboardHelper
import com.benben.bendown_android.data.RecentFilesManager
import com.benben.bendown_android.data.model.MarkdownFile
import com.benben.bendown_android.data.model.RecentFile
import com.benben.bendown_android.data.resolver.DocumentResolver
import com.benben.bendown_android.data.resolver.SimpleDocumentResolver
import com.benben.bendown_android.ui.screens.FileListScreen
import com.benben.bendown_android.ui.screens.MarkdownViewerScreen
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var documentResolver: DocumentResolver
    private lateinit var recentFilesManager: RecentFilesManager
    private lateinit var clipboardHelper: ClipboardHelper

    // 文件选择器 Launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                openFileFromUri(uri, "来自本地")
            }
        }
    }

    // 当前打开的文件
    private var selectedFile by mutableStateOf<MarkdownFile?>(null)

    // 当前文件的初始滚动位置
    private var initialScrollPosition by mutableStateOf(0)

    // 历史记录列表（显示用）
    private var recentFiles by mutableStateOf<List<RecentFile>>(emptyList())

    // 剪贴板内容（用于弹窗显示）
    private var clipboardContent by mutableStateOf<String?>(null)
    private var clipboardFileName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置状态栏图标为深色（适合浅色背景）
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        documentResolver = SimpleDocumentResolver(this)
        recentFilesManager = RecentFilesManager(this)
        clipboardHelper = ClipboardHelper(this)

        // 加载历史记录
        loadRecentFiles()

        setContent {
            MaterialTheme {
                MarkdownReaderApp(
                    documentResolver = documentResolver,
                    selectedFile = selectedFile,
                    initialScrollPosition = initialScrollPosition,
                    recentFiles = recentFiles,
                    clipboardContent = clipboardContent,
                    clipboardFileName = clipboardFileName,
                    onOpenFilePicker = { openFilePicker() },
                    onRecentFileClick = { recentFile -> openRecentFile(recentFile) },
                    onClearHistory = {
                        recentFilesManager.clear()
                        loadRecentFiles()
                    },
                    onScrollPositionChange = { uriString, position ->
                        recentFilesManager.updateScrollPosition(uriString, position)
                    },
                    onBack = {
                        selectedFile = null
                        initialScrollPosition = 0
                        loadRecentFiles()
                    },
                    onSaveClipboard = { saveClipboardContent() },
                    onDismissClipboard = { dismissClipboardDialog() },
                    onShareFile = { shareFile(it) },
                    onRenameFile = { recentFile, newName -> renameFile(recentFile, newName) },
                    onDeleteFile = { deleteFileOrRemoveRecord(it) }
                )
            }
        }

        // 处理初始 Intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    override fun onResume() {
        super.onResume()
        // 不在这里检测，改为 onWindowFocusChanged
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // 只有获得焦点且在首页时才检测
        if (hasFocus && selectedFile == null) {
            // 延迟一点，确保窗口完全激活
            window.decorView.postDelayed({
                checkClipboard()
            }, 100)
        }
    }

    /**
     * 检测剪贴板内容
     */
    private fun checkClipboard() {
        val text = clipboardHelper.getClipboardText()
        if (text.isNullOrBlank()) return

        // 检查是否是 Markdown 或长文本
        val isMarkdown = clipboardHelper.isMarkdownLike(text)
        if (!isMarkdown && text.length < 50) return

        // 检查是否已提示过
        val hash = clipboardHelper.getContentHash(text)
        if (clipboardHelper.hasPrompted(hash)) return

        // 显示弹窗
        clipboardContent = text
        clipboardFileName = clipboardHelper.generateFileName(text)
        clipboardHelper.markPrompted(hash)
    }

    /**
     * 保存剪贴板内容到文件
     */
    private fun saveClipboardContent() {
        val text = clipboardContent ?: return

        try {
            // 保存到 App 私有目录（filesDir）
            val file = File(filesDir, clipboardFileName)
            file.writeText(text)

            // 添加到历史记录并打开（来源：来自剪贴板）
            val uri = Uri.fromFile(file)
            openFileFromUri(uri, "来自剪贴板")

            Toast.makeText(this, "已保存：$clipboardFileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
        }

        // 清除弹窗状态
        clipboardContent = null
    }

    /**
     * 取消保存
     */
    private fun dismissClipboardDialog() {
        clipboardContent = null
    }

    /**
     * 分享文件
     */
    private fun shareFile(recentFile: RecentFile) {
        try {
            val uri = recentFile.uri
            val isLocalFile = recentFile.uriString.startsWith("file:")
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                if (isLocalFile) {
                    // 本地文件：需要通过 FileProvider 分享
                    // 直接分享文本内容
                    val file = java.io.File(uri.path ?: return)
                    val content = file.readText()
                    putExtra(Intent.EXTRA_TEXT, content)
                } else {
                    // content:// URI：分享文件流
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            startActivity(Intent.createChooser(shareIntent, "分享文件"))
        } catch (e: Exception) {
            Toast.makeText(this, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 重命名文件
     */
    private fun renameFile(recentFile: RecentFile, newName: String) {
        val isLocalFile = recentFile.uriString.startsWith("file:")
        
        if (!isLocalFile) {
            Toast.makeText(this, "只能重命名本地保存的文件", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 获取原文件
            val oldPath = recentFile.uri.path
            if (oldPath == null) {
                Toast.makeText(this, "文件路径无效", Toast.LENGTH_SHORT).show()
                return
            }

            val oldFile = File(oldPath)
            if (!oldFile.exists()) {
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show()
                recentFilesManager.remove(recentFile.uriString)
                loadRecentFiles()
                return
            }

            // 构建新文件名（保留扩展名）
            val extension = oldFile.extension
            val newFileName = if (extension.isNotEmpty()) "$newName.$extension" else newName
            val newFile = File(oldFile.parent, newFileName)

            // 重命名
            if (oldFile.renameTo(newFile)) {
                // 更新历史记录（使用 Uri.fromFile 保持格式一致）
                recentFilesManager.updateFileName(recentFile.uriString, newFileName, Uri.fromFile(newFile).toString())
                loadRecentFiles()
                Toast.makeText(this, "已重命名为 $newFileName", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "重命名失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "重命名失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 删除文件或移除记录
     */
    private fun deleteFileOrRemoveRecord(recentFile: RecentFile) {
        val isLocalFile = recentFile.uriString.startsWith("file:")

        if (isLocalFile) {
            // 删除本地文件
            try {
                val path = recentFile.uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists() && file.delete()) {
                        recentFilesManager.remove(recentFile.uriString)
                        loadRecentFiles()
                        Toast.makeText(this, "文件已删除", Toast.LENGTH_SHORT).show()
                    } else {
                        // 文件不存在，只移除记录
                        recentFilesManager.remove(recentFile.uriString)
                        loadRecentFiles()
                        Toast.makeText(this, "记录已移除", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    recentFilesManager.remove(recentFile.uriString)
                    loadRecentFiles()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 只移除记录
            recentFilesManager.remove(recentFile.uriString)
            loadRecentFiles()
            Toast.makeText(this, "记录已移除", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecentFiles() {
        recentFiles = recentFilesManager.getDisplayList()
    }

    private fun openRecentFile(recentFile: RecentFile) {
        // 恢复滚动位置
        initialScrollPosition = recentFile.scrollPosition
        openFileFromUri(recentFile.uri, recentFile.source)
    }

    /**
     * 获取来源 App 名称
     */
    private fun getSourceAppName(intent: Intent): String {
        val myPackageName = packageName
        
        // 1. 尝试从 Activity.getReferrer() 获取（API 22+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val referrerUri = referrer
            if (referrerUri != null && referrerUri.scheme == "android-app") {
                val referrerPackage = referrerUri.host
                if (!referrerPackage.isNullOrBlank() && referrerPackage != myPackageName) {
                    // 排除系统选择器
                    if (!isChooserPackage(referrerPackage)) {
                        try {
                            val appInfo = packageManager.getApplicationInfo(referrerPackage, 0)
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            return "来自$appName"
                        } catch (e: Exception) {
                            // 继续尝试其他方式
                        }
                    }
                }
            }
        }
        
        // 2. 尝试从 Intent.EXTRA_REFERRER 获取
        val referrerExtra = intent.getStringExtra(Intent.EXTRA_REFERRER)
        if (!referrerExtra.isNullOrBlank()) {
            val referrerPackage = referrerExtra
                .removePrefix("android-app://")
                .removeSuffix("/")
            
            if (referrerPackage.isNotBlank() && referrerPackage != myPackageName && !isChooserPackage(referrerPackage)) {
                try {
                    val appInfo = packageManager.getApplicationInfo(referrerPackage, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    return "来自$appName"
                } catch (e: Exception) {
                    // 继续尝试
                }
            }
        }
        
        // 3. 检查是否有 ClipData 包含来源信息
        val clipData = intent.clipData
        if (clipData != null && clipData.itemCount > 0) {
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                item.intent?.`package`?.let { pkg ->
                    if (pkg.isNotBlank() && pkg != myPackageName && !isChooserPackage(pkg)) {
                        try {
                            val appInfo = packageManager.getApplicationInfo(pkg, 0)
                            val appName = packageManager.getApplicationLabel(appInfo).toString()
                            return "来自$appName"
                        } catch (e: Exception) {
                            // 继续
                        }
                    }
                }
            }
        }
        
        // 4. 根据 Intent 类型判断来源
        return when (intent.action) {
            Intent.ACTION_VIEW -> "来自外部打开"
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> "来自分享"
            else -> ""
        }
    }
    
    /**
     * 判断是否是系统选择器包名
     */
    private fun isChooserPackage(packageName: String): Boolean {
        return packageName.contains("chooser", ignoreCase = true) ||
               packageName.contains("intentchooser") ||
               packageName == "com.android.intentchooser" ||
               packageName == "com.google.android.intentchooser"
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    try {
                        val source = getSourceAppName(intent)
                        openFileFromUri(uri, source)
                    } catch (e: Exception) {
                        Toast.makeText(this, "打开文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        selectedFile = null
                    }
                }
            }
            Intent.ACTION_SEND -> {
                val uri = getParcelableExtraCompat<Uri>(intent, Intent.EXTRA_STREAM)
                uri?.let {
                    try {
                        val source = getSourceAppName(intent)
                        openFileFromUri(it, source)
                    } catch (e: Exception) {
                        Toast.makeText(this, "打开文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        selectedFile = null
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = getParcelableArrayListExtraCompat<Uri>(intent, Intent.EXTRA_STREAM)
                uris?.firstOrNull()?.let { uri ->
                    try {
                        val source = getSourceAppName(intent)
                        openFileFromUri(uri, source)
                    } catch (e: Exception) {
                        Toast.makeText(this, "打开文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        selectedFile = null
                    }
                }
            }
        }
    }

    // 兼容新旧 API 的辅助方法
    private inline fun <reified T : android.os.Parcelable> getParcelableExtraCompat(intent: Intent, name: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(name)
        }
    }

    private inline fun <reified T : android.os.Parcelable> getParcelableArrayListExtraCompat(intent: Intent, name: String): ArrayList<T>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(name)
        }
    }

    private fun openFileFromUri(uri: Uri, source: String = "") {
        // 尝试获取持久化权限
        if (uri.scheme == "content") {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // 忽略权限获取失败
            }
        }

        // 获取文件名
        val fileName = documentResolver.getFileName(uri) ?: "未命名文件.md"

        // 检查文件扩展名
        val isValidExtension = fileName.lowercase().let {
            it.endsWith(".md") || it.endsWith(".txt") || it.endsWith(".markdown")
        }

        if (!isValidExtension) {
            selectedFile = MarkdownFile(
                name = fileName,
                uri = uri,
                displayName = "❌ 不支持的文件类型"
            )
            return
        }

        // 对于外部文件（content://），尝试保存到本地
        var finalUri = uri
        var finalSource = source
        var fileSize = 0L
        
        if (uri.scheme == "content") {
            // 检查是否已有本地副本
            val existingRecord = recentFilesManager.getByUri(uri.toString())
            val localUriString = existingRecord?.uriString?.takeIf { it.startsWith("file:") }
            
            if (localUriString != null) {
                // 已有本地副本，直接使用
                finalUri = Uri.parse(localUriString)
                val localFile = File(finalUri.path ?: "")
                if (localFile.exists()) {
                    fileSize = localFile.length()
                    finalSource = existingRecord.source
                }
            } else {
                // 尝试保存到本地
                try {
                    val savedFile = saveExternalToLocal(uri, fileName)
                    if (savedFile != null) {
                        finalUri = Uri.fromFile(savedFile)
                        fileSize = savedFile.length()
                        // 保存成功，提示用户
                        Toast.makeText(this, "已保存到本地：$fileName", Toast.LENGTH_SHORT).show()
                    } else {
                        // 保存失败，使用原URI
                        fileSize = getFileSize(uri)
                    }
                } catch (e: Exception) {
                    // 保存失败，使用原URI
                    fileSize = getFileSize(uri)
                }
            }
        } else if (uri.scheme == "file") {
            // 本地文件，直接获取大小
            fileSize = getFileSize(uri)
        }

        // 获取之前的滚动位置（如果有的话）
        val existingRecord = recentFilesManager.getByUri(uri.toString())
        if (existingRecord != null && initialScrollPosition == 0) {
            initialScrollPosition = existingRecord.scrollPosition
        }

        // 创建 MarkdownFile 对象
        val markdownFile = MarkdownFile(
            name = fileName,
            uri = finalUri,
            displayName = fileName
        )

        // 保存到历史记录
        if (finalSource.isBlank()) {
            finalSource = existingRecord?.source ?: ""
        }
        val recentFile = RecentFile(
            fileName = fileName,
            uriString = finalUri.toString(),
            fileSize = fileSize,
            lastOpenedTime = System.currentTimeMillis(),
            scrollPosition = existingRecord?.scrollPosition ?: 0,
            source = finalSource
        )
        recentFilesManager.add(recentFile)

        // 打开文件
        selectedFile = markdownFile
    }

    /**
     * 将外部文件保存到本地
     * 如果同名文件已存在：
     * - 内容相同：跳过，返回现有文件
     * - 内容不同：加数字后缀保存
     */
    private fun saveExternalToLocal(uri: Uri, fileName: String): File? {
        return try {
            // 读取内容
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val content = inputStream.bufferedReader().readText()
            inputStream.close()
            
            // 检查文件大小（最大10MB）
            if (content.length > 10 * 1024 * 1024) {
                return null
            }
            
            // 检查同名文件是否存在
            var localFile = File(filesDir, fileName)
            if (localFile.exists()) {
                // 比较内容
                val existingContent = localFile.readText()
                if (existingContent == content) {
                    // 内容相同，直接返回现有文件
                    return localFile
                }
                // 内容不同，找一个新的文件名
                val baseName = fileName.substringBeforeLast(".")
                val extension = fileName.substringAfterLast(".", "")
                var index = 1
                while (localFile.exists()) {
                    val newName = if (extension.isNotEmpty()) {
                        "${baseName}_$index.$extension"
                    } else {
                        "${baseName}_$index"
                    }
                    localFile = File(filesDir, newName)
                    index++
                }
            }
            
            // 保存到本地
            localFile.writeText(content)
            localFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            when (uri.scheme) {
                "file" -> {
                    // file:// URI 直接读取文件大小
                    File(uri.path ?: return 0L).length()
                }
                "content" -> {
                    // content:// URI 通过 ContentResolver 查询
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                            if (sizeIndex >= 0) {
                                return cursor.getLong(sizeIndex)
                            }
                        }
                    }
                    0L
                }
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/markdown", "text/plain"))
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        filePickerLauncher.launch(intent)
    }
}

@Composable
fun MarkdownReaderApp(
    documentResolver: DocumentResolver,
    selectedFile: MarkdownFile?,
    initialScrollPosition: Int,
    recentFiles: List<RecentFile>,
    clipboardContent: String?,
    clipboardFileName: String,
    onOpenFilePicker: () -> Unit,
    onRecentFileClick: (RecentFile) -> Unit,
    onClearHistory: () -> Unit,
    onScrollPositionChange: (String, Int) -> Unit,
    onBack: () -> Unit,
    onSaveClipboard: () -> Unit,
    onDismissClipboard: () -> Unit,
    onShareFile: (RecentFile) -> Unit,
    onRenameFile: (RecentFile, String) -> Unit,
    onDeleteFile: (RecentFile) -> Unit
) {
    val context = LocalContext.current

    if (selectedFile == null) {
        Box {
            FileListScreen(
                onOpenFilePicker = onOpenFilePicker,
                recentFiles = recentFiles,
                onRecentFileClick = onRecentFileClick,
                onClearHistory = onClearHistory,
                onShareFile = onShareFile,
                onRenameFile = onRenameFile,
                onDeleteFile = onDeleteFile
            )

            // 剪贴板保存提示弹窗
            if (clipboardContent != null) {
                ClipboardSaveDialog(
                    fileName = clipboardFileName,
                    contentPreview = clipboardContent.take(100) + if (clipboardContent.length > 100) "..." else "",
                    onSave = onSaveClipboard,
                    onDismiss = onDismissClipboard
                )
            }
        }
    } else {
        MarkdownViewerScreen(
            file = selectedFile,
            documentResolver = documentResolver,
            context = context,
            initialScrollPosition = initialScrollPosition,
            onBack = onBack,
            onScrollPositionChange = onScrollPositionChange
        )
    }
}

@Preview
@Composable
fun PreviewApp() {
    val context = LocalContext.current
    MaterialTheme {
        MarkdownReaderApp(
            documentResolver = SimpleDocumentResolver(context),
            selectedFile = null,
            initialScrollPosition = 0,
            recentFiles = emptyList(),
            clipboardContent = null,
            clipboardFileName = "",
            onOpenFilePicker = {},
            onRecentFileClick = {},
            onClearHistory = {},
            onScrollPositionChange = { _, _ -> },
            onBack = {},
            onSaveClipboard = {},
            onDismissClipboard = {},
            onShareFile = {},
            onRenameFile = { _, _ -> },
            onDeleteFile = {}
        )
    }
}

/**
 * 剪贴板保存确认弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardSaveDialog(
    fileName: String,
    contentPreview: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 检测到剪贴板内容",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "文件名：$fileName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contentPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text("保存并打开")
                    }
                }
            }
        }
    }
}
