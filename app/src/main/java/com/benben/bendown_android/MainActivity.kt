package com.benben.bendown_android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.benben.bendown_android.data.RecentFilesManager
import com.benben.bendown_android.data.model.MarkdownFile
import com.benben.bendown_android.data.model.RecentFile
import com.benben.bendown_android.data.resolver.DocumentResolver
import com.benben.bendown_android.data.resolver.SimpleDocumentResolver
import com.benben.bendown_android.ui.screens.FileListScreen
import com.benben.bendown_android.ui.screens.MarkdownViewerScreen

class MainActivity : ComponentActivity() {

    private lateinit var documentResolver: DocumentResolver
    private lateinit var recentFilesManager: RecentFilesManager

    // 文件选择器 Launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                openFileFromUri(uri)
            }
        }
    }

    // 当前打开的文件
    private var selectedFile by mutableStateOf<MarkdownFile?>(null)

    // 历史记录列表
    private var recentFiles by mutableStateOf<List<RecentFile>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentResolver = SimpleDocumentResolver(this)
        recentFilesManager = RecentFilesManager(this)

        // 加载历史记录
        loadRecentFiles()

        setContent {
            MaterialTheme {
                MarkdownReaderApp(
                    documentResolver = documentResolver,
                    selectedFile = selectedFile,
                    recentFiles = recentFiles,
                    onOpenFilePicker = { openFilePicker() },
                    onRecentFileClick = { recentFile -> openRecentFile(recentFile) },
                    onBack = { 
                        selectedFile = null
                        loadRecentFiles()
                    }
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

    private fun loadRecentFiles() {
        recentFiles = recentFilesManager.getAll()
    }

    private fun openRecentFile(recentFile: RecentFile) {
        openFileFromUri(recentFile.uri)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    try {
                        openFileFromUri(uri)
                    } catch (e: Exception) {
                        selectedFile = null
                    }
                }
            }
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let {
                    try {
                        openFileFromUri(it)
                    } catch (e: Exception) {
                        selectedFile = null
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                uris?.firstOrNull()?.let { uri ->
                    try {
                        openFileFromUri(uri)
                    } catch (e: Exception) {
                        selectedFile = null
                    }
                }
            }
        }
    }

    private fun openFileFromUri(uri: Uri) {
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

        // 获取文件大小
        val fileSize = getFileSize(uri)

        // 创建 MarkdownFile 对象
        val markdownFile = MarkdownFile(
            name = fileName,
            uri = uri,
            displayName = fileName
        )

        // 保存到历史记录
        val recentFile = RecentFile(
            fileName = fileName,
            uriString = uri.toString(),
            fileSize = fileSize,
            lastOpenedTime = System.currentTimeMillis()
        )
        recentFilesManager.add(recentFile)

        // 打开文件
        selectedFile = markdownFile
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        return cursor.getLong(sizeIndex)
                    }
                }
            }
            0L
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
    recentFiles: List<RecentFile>,
    onOpenFilePicker: () -> Unit,
    onRecentFileClick: (RecentFile) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    if (selectedFile == null) {
        FileListScreen(
            onOpenFilePicker = onOpenFilePicker,
            recentFiles = recentFiles,
            onRecentFileClick = onRecentFileClick
        )
    } else {
        MarkdownViewerScreen(
            file = selectedFile,
            documentResolver = documentResolver,
            context = context,
            onBack = onBack
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
            recentFiles = emptyList(),
            onOpenFilePicker = {},
            onRecentFileClick = {},
            onBack = {}
        )
    }
}
