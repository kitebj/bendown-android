package com.benben.bendown_android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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

/**
 * 数据模型：Markdown文件信息
 */
data class MarkdownFile(
    val name: String,           // 文件名
    val path: String,           // 文件路径（在assets中的路径）
    val displayName: String     // 显示名称
)

@Composable
fun MarkdownReaderApp() {
    var selectedFile by remember { mutableStateOf<MarkdownFile?>(null) }
    val context = LocalContext.current
    
    // 测试文件列表
    val testFiles = remember {
        listOf(
            MarkdownFile("welcome.md", "welcome.md", "欢迎使用.md"),
            MarkdownFile("roadmap.md", "roadmap.md", "开发计划.md"),
            MarkdownFile("tech_details.md", "tech_details.md", "技术文档.md")
        )
    }
    
    if (selectedFile == null) {
        // 文件列表界面
        FileListScreen(
            files = testFiles,
            onFileSelected = { file -> selectedFile = file }
        )
    } else {
        // 文件内容界面
        FileContentView(
            file = selectedFile!!,
            context = context,
            onBack = { selectedFile = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    files: List<MarkdownFile>,
    onFileSelected: (MarkdownFile) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "BenMarkDown阅读器",
                        fontSize = 18.sp
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Text(
                text = "请选择要查看的文件:",
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(files) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onFileSelected(file) },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F0F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = file.displayName,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "路径: assets/${file.path}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "打开",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileContentView(
    file: MarkdownFile,
    context: Context,
    onBack: () -> Unit
) {
    var fileContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // 读取文件内容
    LaunchedEffect(file) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                val content = withContext(Dispatchers.IO) {
                    readAssetFile(context, file.path)
                }
                
                fileContent = content
            } catch (e: IOException) {
                errorMessage = "读取文件失败: ${e.message}"
            } catch (e: Exception) {
                errorMessage = "发生错误: ${e.message}"
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
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
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
                        MarkdownContentComplete(
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
 * 完整Markdown解析（含链接、删除线、任务列表）
 */
@Composable
fun MarkdownContentComplete(
    content: String,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
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
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color(0xFF2D2D2D),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (language.isNotEmpty()) {
                                Text(
                                    text = language,
                                    color = Color(0xFF90A4AE),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            Text(
                                text = codeLines.joinToString("\n"),
                                color = Color(0xFFE0E0E0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                
                // 标题
                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val titleText = line.substring(level).trim()
                    
                    when (level) {
                        1 -> {
                            Text(
                                text = parseInlineFormatting(titleText, context),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                            )
                        }
                        2 -> {
                            Text(
                                text = parseInlineFormatting(titleText, context),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424242),
                                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                            )
                        }
                        3 -> {
                            Text(
                                text = parseInlineFormatting(titleText, context),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF616161),
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = parseInlineFormatting(titleText, context),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )
                        }
                    }
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
                    
                    Column(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        items.forEach { (checked, item) ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = parseInlineFormatting(item, context),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                                    color = if (checked) Color.Gray else Color.Unspecified
                                )
                            }
                        }
                    }
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
                    
                    Column(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 18.sp,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = parseInlineFormatting(item, context),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
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
                    
                    Column(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 15.sp,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = parseInlineFormatting(item, context),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
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
                        // 渲染引用块
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            quoteGroups.forEach { (level, linesInGroup) ->
                                // 根据层级选择不同的颜色
                                val (bgColor, textColor) = when (level) {
                                    1 -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
                                    2 -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
                                    3 -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
                                    else -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                                }
                                
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = ((level - 1) * 12).dp,
                                            top = if (level > 1) 2.dp else 0.dp
                                        ),
                                    color = bgColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp, 8.dp)
                                    ) {
                                        linesInGroup.forEachIndexed { index, quoteLine ->
                                            Text(
                                                text = parseInlineFormatting(quoteLine, context),
                                                fontStyle = FontStyle.Italic,
                                                color = textColor,
                                                lineHeight = 22.sp
                                            )
                                            if (index < linesInGroup.size - 1) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 普通段落
                else -> {
                    Text(
                        text = parseInlineFormatting(line, context),
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    i++
                }
            }
        }
        
        // 底部添加额外空间
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 解析行内格式（粗体、斜体、代码、链接、删除线）
 */
private fun parseInlineFormatting(text: String, context: Context): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // 删除线 ~~text~~
                i + 1 < text.length && text.substring(i, i + 2) == "~~" -> {
                    i += 2
                    val end = text.indexOf("~~", i)
                    if (end != -1) {
                        withStyle(
                            style = SpanStyle(textDecoration = TextDecoration.LineThrough)
                        ) {
                            append(parseInlineFormatting(text.substring(i, end), context))
                        }
                        i = end + 2
                    } else {
                        append("~~")
                    }
                }
                
                // 链接 [text](url)
                text[i] == '[' -> {
                    val linkEnd = text.indexOf("](", i)
                    if (linkEnd != -1) {
                        val linkText = text.substring(i + 1, linkEnd)
                        val urlStart = linkEnd + 2
                        val urlEnd = text.indexOf(")", urlStart)
                        if (urlEnd != -1) {
                            val url = text.substring(urlStart, urlEnd)
                            
                            // 添加点击链接
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF1976D2),
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(linkText)
                            }
                            
                            // 添加点击事件（使用StringAnnotation）
                            addStringAnnotation(
                                tag = "URL",
                                annotation = url,
                                start = length - linkText.length,
                                end = length
                            )
                            
                            i = urlEnd + 1
                        } else {
                            append(text[i])
                            i++
                        }
                    } else {
                        append(text[i])
                        i++
                    }
                }
                
                // 粗体 **text** 或 __text__
                i + 1 < text.length && (text.substring(i, i + 2) == "**" || text.substring(i, i + 2) == "__") -> {
                    val delimiter = text.substring(i, i + 2)
                    i += 2
                    val end = text.indexOf(delimiter, i)
                    if (end != -1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parseInlineFormatting(text.substring(i, end), context))
                        }
                        i = end + 2
                    } else {
                        append(delimiter)
                    }
                }
                
                // 斜体 *text* 或 _text_
                (text[i] == '*' || text[i] == '_') -> {
                    val delimiter = text[i].toString()
                    i++
                    val end = text.indexOf(delimiter, i)
                    if (end != -1) {
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(parseInlineFormatting(text.substring(i, end), context))
                        }
                        i = end + 1
                    } else {
                        append(delimiter)
                    }
                }
                
                // 行内代码 `code`
                text[i] == '`' -> {
                    i++
                    val end = text.indexOf('`', i)
                    if (end != -1) {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0xFFF5F5F5),
                                color = Color(0xFFD32F2F)
                            )
                        ) {
                            append(text.substring(i, end))
                        }
                        i = end + 1
                    } else {
                        append('`')
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

/**
 * 从assets读取文件
 */
suspend fun readAssetFile(context: Context, fileName: String): String {
    return withContext(Dispatchers.IO) {
        context.assets.open(fileName).use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }
    }
}

@Preview
@Composable
fun PreviewApp() {
    MaterialTheme {
        MarkdownReaderApp()
    }
}
