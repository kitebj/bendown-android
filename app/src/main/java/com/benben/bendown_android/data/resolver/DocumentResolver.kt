package com.benben.bendown_android.data.resolver

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * 文档解析器接口
 */
interface DocumentResolver {
    /**
     * 最大文件大小（10MB）
     */
    val MAX_FILE_SIZE: Long
        get() = 10 * 1024 * 1024 // 10MB

    /**
     * 从 URI 读取文本内容
     * @param uri 文件 URI
     * @return 文件内容字符串
     * @throws IOException 如果文件超过最大大小
     */
    suspend fun readText(uri: Uri): String

    /**
     * 获取文件名
     * @param uri 文件 URI
     * @return 文件名，如果无法获取则返回 null
     */
    fun getFileName(uri: Uri): String?
}

/**
 * 简化版 DocumentResolver 实现（支持 content:// 和 file:// URI）
 */
class SimpleDocumentResolver(
    private val context: Context
) : DocumentResolver {

    override suspend fun readText(uri: Uri): String = withContext(Dispatchers.IO) {
        // 先检查文件大小（通过 ContentResolver）
        val fileSize = getFileSize(uri)
        if (fileSize != null && fileSize > MAX_FILE_SIZE) {
            throw IOException("文件过大（${fileSize / 1024 / 1024}MB），最大支持 ${MAX_FILE_SIZE / 1024 / 1024}MB")
        }
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalArgumentException("无法打开 URI: $uri")
    }

    /**
     * 获取文件大小
     */
    private fun getFileSize(uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        cursor.getLong(sizeIndex)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getFileName(uri: Uri): String? {
        // 先尝试从 content resolver 获取
        var result: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略 query 失败
        }

        // 如果没获取到，尝试从 uri 路径获取
        if (result == null) {
            result = uri.lastPathSegment
        }

        return result
    }
}
