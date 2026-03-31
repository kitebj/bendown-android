package com.benben.bendown_android.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.data.model.MarkdownFile
import com.benben.bendown_android.data.resolver.DocumentResolver
import com.benben.bendown_android.ui.components.MarkdownCodeBlock
import com.benben.bendown_android.ui.components.MarkdownHeading
import com.benben.bendown_android.ui.components.MarkdownOrderedList
import com.benben.bendown_android.ui.components.MarkdownParagraph
import com.benben.bendown_android.ui.components.MarkdownQuote
import com.benben.bendown_android.ui.components.MarkdownTaskList
import com.benben.bendown_android.ui.components.MarkdownUnorderedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Markdown 查看器页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkdownViewerScreen(
    file: MarkdownFile,
    documentResolver: DocumentResolver,
    context: Context,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fileContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // 检查是否是错误提示文件
    val isErrorFile = file.displayName.startsWith("❌")

    // 读取文件内容
    LaunchedEffect(file) {
        if (isErrorFile) {
            // 是错误文件，直接显示错误
            isLoading = false
            errorMessage = "不支持的文件类型：${file.name}\n\n仅支持 .md、.txt、.markdown 格式的文件"
            return@LaunchedEffect
        }

        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                val content = when {
                    file.uri != null -> {
                        // 从 URI 读取（SAF）
                        val rawContent = documentResolver.readText(file.uri!!)
                        
                        // 简单检查是否是文本文件（检查是否包含大量不可打印字符）
                        if (isBinaryContent(rawContent)) {
                            throw IOException("文件似乎是二进制文件，不是文本文件")
                        }
                        
                        rawContent
                    }
                    else -> {
                        throw IllegalArgumentException("文件缺少 uri")
                    }
                }

                fileContent = content
            } catch (e: IOException) {
                errorMessage = "❌ 读取文件失败\n\n${e.message}"
            } catch (e: Exception) {
                errorMessage = "❌ 发生错误\n\n${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        file.displayName,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    // 加载中
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在读取文件...", fontSize = 16.sp)
                    }
                }

                errorMessage != null -> {
                    // 错误
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❌ $errorMessage",
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = onBack) {
                            Text("返回")
                        }
                    }
                }

                fileContent != null -> {
                    // 完整Markdown解析（含链接、删除线、任务列表）
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 信息提示
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = "✨ 新增支持：链接、删除线、任务列表",
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0),
                                modifier = Modifier.padding(8.dp, 4.dp)
                            )
                        }

                        // 内容区域
                        MarkdownContent(
                            content = fileContent!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Markdown 内容渲染
 */
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trimEnd()

            when {
                // 空行
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    i++
                }

                // 水平分隔线 (---, ***, ___)
                (line.trim() == "---" || line.trim() == "***" || line.trim() == "___") -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    i++
                }

                // 代码块 (```)
                line.trim().startsWith("```") -> {
                    val language = line.trim().substring(3).trim()
                    val codeLines = mutableListOf<String>()
                    i++

                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }

                    if (i < lines.size && lines[i].trim().startsWith("```")) {
                        i++
                    }

                    MarkdownCodeBlock(
                        language = language,
                        codeLines = codeLines
                    )
                }

                // 标题
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val titleText = line.substring(level).trim()
                    MarkdownHeading(level = level, text = titleText)
                    i++
                }

                // 任务列表 (- [ ] 或 - [x])
                line.trim().startsWith("- [ ] ") || line.trim().startsWith("- [x] ") ||
                line.trim().startsWith("* [ ] ") || line.trim().startsWith("* [x] ") -> {
                    val items = mutableListOf<Pair<Boolean, String>>()

                    while (i < lines.size && (
                        lines[i].trim().startsWith("- [ ] ") ||
                        lines[i].trim().startsWith("- [x] ") ||
                        lines[i].trim().startsWith("* [ ] ") ||
                        lines[i].trim().startsWith("* [x] "))
                    ) {
                        val trimmed = lines[i].trim()
                        val isChecked = trimmed.contains("[x]") || trimmed.contains("[X]")
                        val textStart = if (trimmed.indexOf("] ") != -1) trimmed.indexOf("] ") + 2 else 0
                        val itemText = if (textStart < trimmed.length) trimmed.substring(textStart) else ""
                        items.add(Pair(isChecked, itemText))
                        i++
                    }

                    MarkdownTaskList(items = items)
                }

                // 无序列表 (- 或 *)
                (line.startsWith("- ") || line.startsWith("* ")) &&
                !line.trim().startsWith("- [ ] ") &&
                !line.trim().startsWith("- [x] ") &&
                !line.trim().startsWith("* [ ] ") &&
                !line.trim().startsWith("* [x] ") -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && (
                        (lines[i].startsWith("- ") || lines[i].startsWith("* ")) &&
                        !lines[i].trim().startsWith("- [ ] ") &&
                        !lines[i].trim().startsWith("- [x] ") &&
                        !lines[i].trim().startsWith("* [ ] ") &&
                        !lines[i].trim().startsWith("* [x] "))
                    ) {
                        items.add(lines[i].trim().substring(2))
                        i++
                    }

                    MarkdownUnorderedList(items = items)
                }

                // 有序列表 (1. 2. 3.)
                line.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val items = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s.*"))) {
                        val trimmedLine = lines[i].trim()
                        val itemText = trimmedLine.substring(trimmedLine.indexOf('.') + 2)
                        items.add(itemText)
                        i++
                    }

                    MarkdownOrderedList(items = items)
                }

                // 引用块 (> )
                line.trim().startsWith(">") -> {
                    // 解析引用块及其嵌套 - 支持多行合并
                    val quoteGroups = mutableListOf<Pair<Int, MutableList<String>>>()
                    var currentLevel = 0
                    var currentLines = mutableListOf<String>()

                    while (i < lines.size && lines[i].trim().startsWith(">")) {
                        val trimmed = lines[i].trim()

                        // 计算嵌套层级（计算有多少个 >）
                        var level = 0
                        var temp = trimmed
                        while (temp.isNotEmpty() && temp[0] == '>') {
                            level++
                            temp = temp.substring(1).trimStart()
                        }

                        // 提取文本内容
                        val text = temp.trim()

                        // 处理层级变化
                        if (level != currentLevel && currentLines.isNotEmpty()) {
                            // 保存前一个组
                            quoteGroups.add(Pair(currentLevel, currentLines))
                            currentLines = mutableListOf()
                        }

                        currentLevel = level
                        if (text.isNotEmpty()) {
                            currentLines.add(text)
                        }
                        i++
                    }

                    // 保存最后一个组
                    if (currentLines.isNotEmpty()) {
                        quoteGroups.add(Pair(currentLevel, currentLines))
                    }

                    if (quoteGroups.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            quoteGroups.forEach { (level, linesInGroup) ->
                                MarkdownQuote(
                                    level = level,
                                    lines = linesInGroup
                                )
                            }
                        }
                    }
                }

                // 普通段落
                else -> {
                    MarkdownParagraph(text = line)
                    i++
                }
            }
        }

        // 底部添加额外空间
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 检查内容是否是二进制文件
 */
private fun isBinaryContent(content: String): Boolean {
    // 检查前 1000 个字符中是否有大量不可打印字符
    val sample = content.take(1000)
    val nonPrintableCount = sample.count { it.isISOControl() && it != '\n' && it != '\r' && it != '\t' }
    return nonPrintableCount > sample.length * 0.3 // 如果超过 30% 是不可打印字符，认为是二进制
}
