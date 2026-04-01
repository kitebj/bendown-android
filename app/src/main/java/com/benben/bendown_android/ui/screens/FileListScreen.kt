package com.benben.bendown_android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.benben.bendown_android.R
import com.benben.bendown_android.data.model.RecentFile
import com.benben.bendown_android.ui.components.SettingsBottomSheet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 文件选择页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    onOpenFilePicker: () -> Unit,
    recentFiles: List<RecentFile> = emptyList(),
    onRecentFileClick: (RecentFile) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onShareFile: (RecentFile) -> Unit = {},
    onRenameFile: (RecentFile, String) -> Unit = { _, _ -> },
    onDeleteFile: (RecentFile) -> Unit = {},
    isClipboardMonitorEnabled: Boolean = true,
    onClipboardMonitorChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // 长按菜单状态
    var selectedItem by remember { mutableStateOf<RecentFile?>(null) }
    var showItemMenu by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }

    // 重命名弹窗状态
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    
    // 获取屏幕尺寸
    val view = LocalView.current
    val density = LocalDensity.current
    val screenWidth = with(density) { view.width.toDp() }
    val screenHeight = with(density) { view.height.toDp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📚", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BenDown - 笨蛋阅读器", fontSize = 18.sp)
                    }
                },
                actions = {
                    // 汉堡菜单
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.Menu, "菜单")
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
                            text = "打开文件",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMenu = false
                                    onOpenFilePicker()
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                        if (recentFiles.isNotEmpty()) {
                            Text(
                                text = "清除记录",
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMenu = false
                                        showClearDialog = true
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                        Text(
                            text = "设置",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMenu = false
                                    showSettings = true
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                        Text(
                            text = "关于",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMenu = false
                                    showAboutDialog = true
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (recentFiles.isEmpty()) {
                // 无历史记录：居中显示图标、欢迎语和按钮
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 显示图标
                        Text(
                            text = "📖",
                            fontSize = 72.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // 欢迎语
                        Text(
                            text = " Markdown",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "简洁之美，纯粹阅读",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "在这里，文字不被打扰",
                            fontSize = 13.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // 打开文件按钮
                        Button(
                            onClick = onOpenFilePicker,
                            modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            Text("打开文件", fontSize = 16.sp)
                        }
                    }
                }
            } else {
                // 有历史记录：只显示历史列表
                Text(
                    text = "最近打开",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(recentFiles) { recentFile ->
                        RecentFileItem(
                            recentFile = recentFile,
                            onClick = { onRecentFileClick(recentFile) },
                            onLongClick = { position ->
                                selectedItem = recentFile
                                menuPosition = position
                                showItemMenu = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 长按菜单
    if (showItemMenu && selectedItem != null) {
        val item = selectedItem!!
        val isLocalFile = item.uriString.startsWith("file:")
        
        // 使用 Box 在触摸点位置放置菜单
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showItemMenu = false
                            selectedItem = null
                        }
                    )
                }
        ) {
            AnimatedVisibility(
                visible = showItemMenu,
                enter = scaleIn(initialScale = 0.8f),
                exit = scaleOut(targetScale = 0.8f)
            ) {
                val menuWidthPx = with(density) { 140.dp.roundToPx() }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            // menuPosition 已经是像素值
                            // x: 菜单右边缘对齐触摸点左边一点，往左弹出
                            // y: 菜单显示在触摸点下方
                            IntOffset(
                                x = (menuPosition.x - menuWidthPx + 20).toInt().coerceAtLeast(16),
                                y = (menuPosition.y + 10).toInt()
                            )
                        }
                        .width(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    tonalElevation = 2.dp,
                    color = Color.White
                ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // 分享
                    Text(
                        text = "分享...",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showItemMenu = false
                                onShareFile(item)
                                selectedItem = null
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                    
                    // 编辑名称（仅本地文件显示）
                    if (isLocalFile) {
                        Text(
                            text = "重命名",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showItemMenu = false
                                    val nameWithoutExt = item.fileName.substringBeforeLast(".")
                                    renameText = nameWithoutExt
                                    selectedItem = item
                                    showRenameDialog = true
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFFE0E0E0)
                    )
                    
                    // 删除文件/移除记录
                    Text(
                        text = if (isLocalFile) "删除文件" else "移除记录",
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showItemMenu = false
                                onDeleteFile(item)
                                selectedItem = null
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
            }
        }
    }

    // 重命名弹窗
    if (showRenameDialog && selectedItem != null) {
        val item = selectedItem!!
        AlertDialog(
            onDismissRequest = { 
                showRenameDialog = false
                selectedItem = null
            },
            title = { Text("编辑名称") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    placeholder = { Text("输入文件名") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newName = renameText.trim()
                        if (newName.isNotEmpty()) {
                            onRenameFile(item, newName)
                        }
                        showRenameDialog = false
                        selectedItem = null
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = false
                        selectedItem = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 清除确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除记录") },
            text = { Text("确定要清除所有历史记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearHistory()
                    }
                ) {
                    Text("确定", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 关于弹窗
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // 设置抽屉
    if (showSettings) {
        SettingsBottomSheet(
            isClipboardMonitorEnabled = isClipboardMonitorEnabled,
            onClipboardMonitorChange = onClipboardMonitorChange,
            onDismiss = { showSettings = false }
        )
    }
}

/**
 * 历史文件列表项（紧凑布局）
 */
@Composable
fun RecentFileItem(
    recentFile: RecentFile,
    onClick: () -> Unit,
    onLongClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取 item 在屏幕上的位置
    var itemPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                // 获取 item 左上角在屏幕上的位置
                itemPositionInRoot = coordinates.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { tapOffset ->
                        // tapOffset 是相对于 item 左上角的偏移
                        // 计算触摸点在屏幕上的绝对位置
                        val absolutePosition = Offset(
                            itemPositionInRoot.x + tapOffset.x,
                            itemPositionInRoot.y + tapOffset.y
                        )
                        onLongClick(absolutePosition)
                    }
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 文件图标
        Text(
            text = "📄",
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        // 文件信息
        Column(modifier = Modifier.weight(1f)) {
            // 文件名
            Text(
                text = recentFile.fileName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 大小、来源、时间
            val infoText = if (recentFile.source.isNotBlank()) {
                "${recentFile.getFormattedSize()} · ${recentFile.source} · ${formatTime(recentFile.lastOpenedTime)}"
            } else {
                "${recentFile.getFormattedSize()} · ${formatTime(recentFile.lastOpenedTime)}"
            }
            Text(
                text = infoText,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }

    // 分隔线
    HorizontalDivider(
        modifier = Modifier.padding(start = 38.dp, end = 12.dp),
        thickness = 0.5.dp,
        color = Color(0xFFE0E0E0)
    )
}

/**
 * 格式化时间显示
 */
private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> {
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * 关于弹窗
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧图片 (1/3 宽度)
                Image(
                    painter = painterResource(id = R.drawable.bendown3),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                )

                // 右侧信息 (2/3 宽度)
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // APP 名称
                    Text(
                        text = "BenDown - 笨蛋阅读器",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 版本号
                    Text(
                        text = "版本：0.2.8",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 最后更新日期
                    Text(
                        text = "更新：2026-03-31",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 作者
                    Text(
                        text = "作者：笨笨 + AI助手",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}
