package com.benben.bendown_android.data

import android.content.ClipboardManager
import android.content.Context
import java.security.MessageDigest

/**
 * 剪贴板管理器
 */
class ClipboardHelper(private val context: Context) {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val prefs = context.getSharedPreferences("clipboard_history", Context.MODE_PRIVATE)

    /**
     * 获取剪贴板文本内容
     */
    fun getClipboardText(): String? {
        val clip = clipboardManager.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).text?.toString()?.trim()
    }

    /**
     * 判断文本是否像 Markdown（包含常见 MD 符号）
     */
    fun isMarkdownLike(text: String): Boolean {
        // 简单检测：包含常见 Markdown 特征
        val indicators = listOf(
            "#",        // 标题
            "**",       // 粗体
            "* ",       // 斜体或列表
            "- ",       // 列表
            "+ ",       // 列表
            "1. ",      // 有序列表
            "> ",       // 引用
            "```",      // 代码块
            "](http",   // 链接
            "](https"   // 链接
        )
        
        return indicators.any { text.contains(it) }
    }

    /**
     * 计算内容的 hash（用于去重）
     */
    fun getContentHash(text: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 检查是否已提示过该内容
     */
    fun hasPrompted(hash: String): Boolean {
        return prefs.contains("prompt_$hash")
    }

    /**
     * 标记已提示过
     */
    fun markPrompted(hash: String) {
        prefs.edit().putBoolean("prompt_$hash", true).apply()
    }

    /**
     * 从内容生成文件名（取第一行，限制长度）
     */
    fun generateFileName(text: String): String {
        val firstLine = text.lines().firstOrNull()?.trim() ?: "未命名"
        // 移除文件名非法字符
        val cleaned = firstLine.replace(Regex("[\\\\/:*?\"<>|]"), "")
        // 截断到 15 个字符
        val truncated = if (cleaned.length > 15) cleaned.substring(0, 15) else cleaned
        return if (truncated.isBlank()) "未命名.md" else "$truncated.md"
    }
}
