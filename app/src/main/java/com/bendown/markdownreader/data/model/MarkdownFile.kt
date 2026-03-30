package com.bendown.markdownreader.data.model

/**
 * Markdown文件数据模型
 */
data class MarkdownFile(
    val name: String,          // 文件名
    val path: String,          // 文件路径
    val size: Long = 0,        // 文件大小（字节）
    val lastModified: Long = 0 // 最后修改时间
) {
    // 获取文件扩展名
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()
    
    // 判断是否为Markdown文件
    val isMarkdown: Boolean
        get() = extension == "md" || extension == "markdown"
    
    // 显示名称（不带扩展名）
    val displayName: String
        get() = name.substringBeforeLast('.')
}