package com.benben.bendown_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 底部抽屉式设置面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isClipboardMonitorEnabled: Boolean,
    onClipboardMonitorChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // 标题栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "设置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 剪贴板监控开关项
            SettingSwitchItem(
                title = "剪贴板监控",
                description = "切换到App时自动检测剪贴板内容",
                icon = "📋",
                checked = isClipboardMonitorEnabled,
                onCheckedChange = onClipboardMonitorChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 关闭按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "关闭",
                    fontSize = 15.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 设置开关项
 */
@Composable
private fun SettingSwitchItem(
    title: String,
    description: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        // 标题和描述
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // 开关
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1976D2),
                checkedTrackColor = Color(0xFFBBDEFB)
            )
        )
    }
}
