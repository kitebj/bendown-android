package com.benben.bendown_android.data.model

import android.net.Uri

/**
 * 数据模型：Markdown文件信息
 */
data class MarkdownFile(
    val name: String,           // 文件名
    val path: String? = null,   // 文件路径（在assets中的路径，可选）
    val uri: Uri? = null,       // 文件 URI（可选，用于 SAF）
    val displayName: String     // 显示名称
) {
    // 兼容旧构造函数（用于 assets 文件）
    constructor(name: String, path: String, displayName: String) : this(
        name = name,
        path = path,
        uri = null,
        displayName = displayName
    )
}

