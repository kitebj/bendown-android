package com.benben.bendown_android.data.model

import android.net.Uri

/**
 * 数据模型：Markdown文件信息
 */
data class MarkdownFile(
    val name: String,           // 文件名
    val uri: Uri,               // 文件 URI（SAF）
    val displayName: String     // 显示名称
)
