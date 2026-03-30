package com.bendown.markdownreader.utils

import android.content.Context
import android.net.Uri
import com.bendown.markdownreader.data.model.MarkdownFile
import java.io.File

/**
 * 文件操作工具类
 */
object FileUtils {
    
    /**
     * 获取指定目录下的Markdown文件列表
     * @param directoryPath 目录路径（如："/sdcard/Documents"）
     * @return Markdown文件列表
     */
    fun getMarkdownFiles(directoryPath: String): List<MarkdownFile> {
        return try {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                return emptyList()
            }
            
            directory.listFiles()?.filter { file ->
                val extension = file.name.substringAfterLast('.', "").lowercase()
                extension == "md" || extension == "markdown"
            }?.map { file ->
                MarkdownFile(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified()
                )
            } ?: emptyList()
            
        } catch (e: SecurityException) {
            // 没有文件访问权限
            emptyList()
        } catch (e: Exception) {
            // 其他异常
            emptyList()
        }
    }
    
    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容，如果读取失败返回null
     */
    fun readFileContent(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (file.exists() && file.canRead()) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 检查文件是否存在
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
    
    /**
     * 获取文件大小（人类可读格式）
     */
    fun getReadableFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var fileSize = size.toDouble()
        var unitIndex = 0
        
        while (fileSize >= 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }
        
        return "%.2f %s".format(fileSize, units[unitIndex])
    }
    
    /**
     * 获取最后修改时间（人类可读格式）
     */
    fun getReadableLastModified(lastModified: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - lastModified
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 30 -> "${days / 30}个月前"
            days > 0 -> "${days}天前"
            hours > 0 -> "${hours}小时前"
            minutes > 0 -> "${minutes}分钟前"
            else -> "刚刚"
        }
    }
}