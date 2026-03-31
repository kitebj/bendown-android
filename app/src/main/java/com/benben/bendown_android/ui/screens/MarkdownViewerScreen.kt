package com.benben.bendown_android.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.data.formatFileSize
import com.benben.bendown_android.data.model.MarkdownFile
import com.benben.bendown_android.data.resolver.DocumentResolver
import com.benben.bendown_android.ui.components.MarkdownCodeBlock
import com.benben.bendown_android.ui.components.MarkdownHeading
import com.benben.bendown_android.ui.components.MarkdownOrderedList
import com.benben.bendown_android.ui.components.MarkdownParagraph
import com.benben.bendown_android.ui.components.MarkdownQuote
import com.benben.bendown_android.ui.components.MarkdownTaskList
import com.benben.bendown_android.ui.components.MarkdownTable
import com.benben.bendown_android.ui.components.MarkdownUnorderedList
import com.benben.bendown_android.ui.components.TableAlignment
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
    initialScrollPosition: Int = 0,
    onBack: () -> Unit,
    onScrollPositionChange: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var fileContent by remember { mutableStateOf<String?>(null) }
    var fileSize by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    
    // 滚动状态（提取到外层以便共享）
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 检查是否是错误提示文件
    val isErrorFile = file.displayName.startsWith("❌")

    // 拦截系统后退键
    BackHandler(enabled = true) {
        onBack()
    }

    // 分享文件
    fun shareFile() {
        file.uri?.let { uri ->
            try {
                val isLocalFile = uri.toString().startsWith("file:")
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/markdown"
                    if (isLocalFile) {
                        // 本地文件：直接分享文本内容
                        val filePath = uri.path
                        if (filePath != null) {
                            val content = java.io.File(filePath).readText()
                            putExtra(Intent.EXTRA_TEXT, content)
                        }
                    } else {
                        // content:// URI：分享文件流
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                context.startActivity(Intent.createChooser(shareIntent, "分享文件"))
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "分享失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 回到顶部
    fun scrollToTop() {
        scope.launch {
            listState.animateScrollToItem(0)
        }
    }

    // 读取文件内容
    LaunchedEffect(file) {
        if (isErrorFile) {
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
                        val rawContent = documentResolver.readText(file.uri!!)
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
                fileSize = content.length.toLong()
            } catch (e: java.lang.SecurityException) {
                // 权限丢失，通常是从微信/QQ分享的文件
                errorMessage = "❌ 无法访问此文件\n\n此文件来自其他应用分享，访问权限已失效。\n\n建议：请从原应用重新分享文件。"
            } catch (e: java.lang.IllegalArgumentException) {
                // URI 无法打开
                errorMessage = "❌ 文件已不可访问\n\n${e.message}\n\n此文件可能已被删除或移动。"
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
                        maxLines = 1,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { scrollToTop() }
                            )
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 三点菜单
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "菜单")
                    }

                    // 下拉菜单（长按菜单风格）
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(
                                Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "分享...",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMenu = false
                                    shareFile()
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                        Text(
                            text = "关闭",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMenu = false
                                    onBack()
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
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
                    MarkdownContentWithStatus(
                        content = fileContent!!,
                        fileSize = fileSize,
                        listState = listState,
                        initialScrollPosition = initialScrollPosition,
                        onScrollPositionChange = { position ->
                            file.uri?.let { uri ->
                                onScrollPositionChange(uri.toString(), position)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 带状态条的 Markdown 内容
 */
@Composable
fun MarkdownContentWithStatus(
    content: String,
    fileSize: Long,
    listState: LazyListState,
    initialScrollPosition: Int = 0,
    onScrollPositionChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    val scope = rememberCoroutineScope()

    // 恢复初始滚动位置
    LaunchedEffect(initialScrollPosition, content) {
        if (initialScrollPosition > 0) {
            kotlinx.coroutines.delay(100)
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems > 1) {
                val targetItem = (initialScrollPosition * (totalItems - 1) / 100).coerceIn(0, totalItems - 1)
                listState.scrollToItem(targetItem)
            }
        }
    }

    // 计算阅读进度（带小数）
    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount

            // 如果没有内容，返回 0%
            if (totalItems == 0) {
                0.0
            } else if (!listState.canScrollForward && listState.canScrollBackward) {
                // 已经到底了（可以往上滚，不能往下滚）
                100.0
            } else if (!listState.canScrollBackward && !listState.canScrollForward) {
                // 内容不足一屏
                100.0
            } else {
                // 使用第一个可见 item 计算进度（更能代表当前阅读位置）
                val firstVisibleItem = listState.firstVisibleItemIndex
                // 总数减1是因为 index 从 0 开始
                if (totalItems > 1) {
                    (firstVisibleItem * 100.0 / (totalItems - 1)).coerceIn(0.0, 100.0)
                } else {
                    100.0
                }
            }
        }
    }

    // 退出时保存滚动位置（取整保存）
    DisposableEffect(Unit) {
        onDispose {
            onScrollPositionChange(scrollProgress.toInt())
        }
    }

    Box(modifier = modifier) {
        // 内容区域（可选择文字）
        SelectionContainer {
            MarkdownContentLazy(
                content = content,
                listState = listState,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 滚动条
        SimpleScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .fillMaxHeight(0.7f)
        )

        // 底部状态条
        ReadingStatusBar(
            fileSize = fileSize,
            progress = scrollProgress,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 简单滚动条（滚动时显示，停止后自动隐藏）
 */
@Composable
fun SimpleScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    // 是否正在滚动
    val isScrollInProgress = listState.isScrollInProgress

    // 动画透明度
    val alpha by animateFloatAsState(
        targetValue = if (isScrollInProgress) 0.5f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "scrollbar_alpha"
    )

    // 滚动进度 (0f - 1f)
    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems <= 1) return@derivedStateOf 0f

            val firstVisibleItem = listState.firstVisibleItemIndex.toFloat()
            firstVisibleItem / (totalItems - 1)
        }
    }

    Box(
        modifier = modifier
            .width(3.dp)
            .padding(vertical = 4.dp)
    ) {
        if (alpha > 0.01f) {
            // 滚动条滑块
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f) // 滑块占总高度的15%
                    .graphicsLayer {
                        translationY = scrollProgress * (size.height * 0.85f)
                        this.alpha = alpha
                    }
                    .background(Color(0xFF666666), RoundedCornerShape(1.5.dp))
            )
        }
    }
}

/**
 * 底部阅读状态条
 */
@Composable
fun ReadingStatusBar(
    fileSize: Long,
    progress: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件大小
            Text(
                text = fileSize.formatFileSize(),
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )

            // 阅读进度（两位小数）
            Text(
                text = String.format("%.2f%%", progress),
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

/**
 * Markdown 内容渲染（LazyColumn 版本）
 */
@Composable
fun MarkdownContentLazy(
    content: String,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()

    LazyColumn(
        state = listState,
        modifier = modifier.padding(16.dp)
    ) {
        // 解析并渲染每一行
        var i = 0
        val items = mutableListOf<MarkdownItem>()

        while (i < lines.size) {
            val line = lines[i].trimEnd()

            when {
                line.isBlank() -> {
                    items.add(MarkdownItem.Spacer(8.dp))
                    i++
                }

                (line.trim() == "---" || line.trim() == "***" || line.trim() == "___") -> {
                    items.add(MarkdownItem.Divider)
                    i++
                }

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

                    items.add(MarkdownItem.Code(language, codeLines))
                }

                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val titleText = line.substring(level).trim()
                    items.add(MarkdownItem.Heading(level, titleText))
                    i++
                }

                line.trim().startsWith("- [ ] ") || line.trim().startsWith("- [x] ") ||
                line.trim().startsWith("* [ ] ") || line.trim().startsWith("* [x] ") -> {
                    val taskItems = mutableListOf<Pair<Boolean, String>>()

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
                        taskItems.add(Pair(isChecked, itemText))
                        i++
                    }

                    items.add(MarkdownItem.TaskList(taskItems))
                }

                (line.startsWith("- ") || line.startsWith("* ")) &&
                !line.trim().startsWith("- [ ] ") &&
                !line.trim().startsWith("- [x] ") &&
                !line.trim().startsWith("* [ ] ") &&
                !line.trim().startsWith("* [x] ") -> {
                    val listItems = mutableListOf<String>()
                    while (i < lines.size && (
                        (lines[i].startsWith("- ") || lines[i].startsWith("* ")) &&
                        !lines[i].trim().startsWith("- [ ] ") &&
                        !lines[i].trim().startsWith("- [x] ") &&
                        !lines[i].trim().startsWith("* [ ] ") &&
                        !lines[i].trim().startsWith("* [x] "))
                    ) {
                        listItems.add(lines[i].trim().substring(2))
                        i++
                    }

                    items.add(MarkdownItem.UnorderedList(listItems))
                }

                line.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val listItems = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s.*"))) {
                        val trimmedLine = lines[i].trim()
                        val itemText = trimmedLine.substring(trimmedLine.indexOf('.') + 2)
                        listItems.add(itemText)
                        i++
                    }

                    items.add(MarkdownItem.OrderedList(listItems))
                }

                line.trim().startsWith(">") -> {
                    val quoteGroups = mutableListOf<Pair<Int, MutableList<String>>>()
                    var currentLevel = 0
                    var currentLines = mutableListOf<String>()

                    while (i < lines.size && lines[i].trim().startsWith(">")) {
                        val trimmed = lines[i].trim()
                        var level = 0
                        var temp = trimmed
                        while (temp.isNotEmpty() && temp[0] == '>') {
                            level++
                            temp = temp.substring(1).trimStart()
                        }

                        val text = temp.trim()

                        if (level != currentLevel && currentLines.isNotEmpty()) {
                            quoteGroups.add(Pair(currentLevel, currentLines))
                            currentLines = mutableListOf()
                        }

                        currentLevel = level
                        if (text.isNotEmpty()) {
                            currentLines.add(text)
                        }
                        i++
                    }

                    if (currentLines.isNotEmpty()) {
                        quoteGroups.add(Pair(currentLevel, currentLines))
                    }

                    items.add(MarkdownItem.Quote(quoteGroups))
                }

                // 表格 (| ... |)
                line.trim().startsWith("|") && line.trim().endsWith("|") -> {
                    val tableLines = mutableListOf<String>()

                    // 收集所有连续的表格行
                    while (i < lines.size &&
                        lines[i].trim().startsWith("|") &&
                        lines[i].trim().endsWith("|")) {
                        tableLines.add(lines[i].trim())
                        i++
                    }

                    // 解析表格
                    if (tableLines.size >= 2 && isTableDivider(tableLines[1])) {
                        // 第一行是表头
                        val headers = parseTableRow(tableLines[0])

                        // 第二行是分隔符，解析对齐方式
                        val alignments = parseTableAlignment(tableLines[1])

                        // 剩余行是数据
                        val rows = tableLines.drop(2).map { parseTableRow(it) }

                        items.add(MarkdownItem.Table(headers, alignments, rows))
                    } else {
                        // 不是有效的表格，当作普通段落处理
                        tableLines.forEach {
                            items.add(MarkdownItem.Paragraph(it))
                        }
                    }
                }

                else -> {
                    items.add(MarkdownItem.Paragraph(line))
                    i++
                }
            }
        }

        // 渲染 items
        items(items.size) { index ->
            when (val item = items[index]) {
                is MarkdownItem.Spacer -> Spacer(modifier = Modifier.height(item.height))
                is MarkdownItem.Divider -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                is MarkdownItem.Code -> MarkdownCodeBlock(language = item.language, codeLines = item.lines)
                is MarkdownItem.Heading -> MarkdownHeading(level = item.level, text = item.text)
                is MarkdownItem.TaskList -> MarkdownTaskList(items = item.items)
                is MarkdownItem.UnorderedList -> MarkdownUnorderedList(items = item.items)
                is MarkdownItem.OrderedList -> MarkdownOrderedList(items = item.items)
                is MarkdownItem.Quote -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        item.groups.forEach { (level, linesInGroup) ->
                            MarkdownQuote(level = level, lines = linesInGroup)
                        }
                    }
                }
                is MarkdownItem.Table -> MarkdownTable(
                    headers = item.headers,
                    alignments = item.alignments,
                    rows = item.rows
                )
                is MarkdownItem.Paragraph -> MarkdownParagraph(text = item.text)
            }
        }

        // 底部空白
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Markdown 项目类型
 */
private sealed class MarkdownItem {
    data class Spacer(val height: androidx.compose.ui.unit.Dp) : MarkdownItem()
    data object Divider : MarkdownItem()
    data class Code(val language: String, val lines: List<String>) : MarkdownItem()
    data class Heading(val level: Int, val text: String) : MarkdownItem()
    data class TaskList(val items: List<Pair<Boolean, String>>) : MarkdownItem()
    data class UnorderedList(val items: List<String>) : MarkdownItem()
    data class OrderedList(val items: List<String>) : MarkdownItem()
    data class Quote(val groups: List<Pair<Int, MutableList<String>>>) : MarkdownItem()
    data class Table(
        val headers: List<String>,
        val alignments: List<TableAlignment>,
        val rows: List<List<String>>
    ) : MarkdownItem()
    data class Paragraph(val text: String) : MarkdownItem()
}

/**
 * 检查内容是否是二进制文件
 */
private fun isBinaryContent(content: String): Boolean {
    val sample = content.take(1000)
    val nonPrintableCount = sample.count { it.isISOControl() && it != '\n' && it != '\r' && it != '\t' }
    return nonPrintableCount > sample.length * 0.3
}

/**
 * 解析表格行
 */
private fun parseTableRow(line: String): List<String> {
    return line.trim()
        .trim('|')
        .split("|")
        .map { it.trim() }
}

/**
 * 解析表格对齐方式
 */
private fun parseTableAlignment(line: String): List<TableAlignment> {
    return line.trim()
        .trim('|')
        .split("|")
        .map { cell ->
            val trimmed = cell.trim()
            when {
                trimmed.startsWith(":") && trimmed.endsWith(":") -> TableAlignment.CENTER
                trimmed.endsWith(":") -> TableAlignment.RIGHT
                else -> TableAlignment.LEFT
            }
        }
}

/**
 * 检查是否是表格分隔行
 */
private fun isTableDivider(line: String): Boolean {
    val trimmed = line.trim().trim('|')
    return trimmed.split("|").all { cell ->
        cell.trim().all { it == '-' || it == ':' || it == ' ' }
    }
}
