package com.benben.bendown_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("BenMarkDown阅读器", fontSize = 18.sp)
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
            // 选择文件按钮
            Button(
                onClick = onOpenFilePicker,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("选择本地文件", fontSize = 15.sp)
            }

            // 历史记录列表
            if (recentFiles.isNotEmpty()) {
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
            } else {
                // 没有历史记录时显示提示
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📖", fontSize = 40.sp)
                        Text(
                            text = "暂无历史记录",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "选择文件后会显示在这里",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
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
