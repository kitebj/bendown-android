package com.benben.bendown_android.data

/**
 * 文件大小格式化扩展函数
 */
fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> String.format("%.1f KB", this / 1024.0)
        this < 1024 * 1024 * 1024 -> String.format("%.1f MB", this / (1024.0 * 1024))
        else -> String.format("%.1f GB", this / (1024.0 * 1024 * 1024))
    }
}
