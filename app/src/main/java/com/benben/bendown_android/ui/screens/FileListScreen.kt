package com.benben.bendown_android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benben.bendown_android.data.model.RecentFile
import java.text.SimpleDateFormat
import java.util.*

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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

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

                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("打开文件", modifier = Modifier.padding(start = 2.dp)) },
                            leadingIcon = { Text("📂", fontSize = 16.sp, modifier = Modifier.padding(end = 0.dp)) },
                            onClick = {
                                showMenu = false
                                onOpenFilePicker()
                            },
                            modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp)
                        )
                        // 只有有历史记录时才显示清除记录
                        if (recentFiles.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("清除记录", modifier = Modifier.padding(start = 2.dp)) },
                                leadingIcon = { Text("🗑️", fontSize = 16.sp, modifier = Modifier.padding(end = 0.dp)) },
                                onClick = {
                                    showMenu = false
                                    showClearDialog = true
                                },
                                modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp)
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("设置", modifier = Modifier.padding(start = 2.dp)) },
                            leadingIcon = { Text("⚙️", fontSize = 16.sp, modifier = Modifier.padding(end = 0.dp)) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("关于", modifier = Modifier.padding(start = 2.dp)) },
                            leadingIcon = { Text("ℹ️", fontSize = 16.sp, modifier = Modifier.padding(end = 0.dp)) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "功能开发中", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp)
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
                            onClick = { onRecentFileClick(recentFile) }
                        )
                    }
                }
            }
        }
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
}

/**
 * 历史文件列表项（紧凑布局）
 */
@Composable
fun RecentFileItem(
    recentFile: RecentFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            // 大小和时间
            Text(
                text = "${recentFile.getFormattedSize()} · ${formatTime(recentFile.lastOpenedTime)}",
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
