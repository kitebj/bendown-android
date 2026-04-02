package com.benben.bendown_android.data.model

import android.net.Uri
import com.benben.bendown_android.data.formatFileSize
import org.json.JSONObject

/**
 * 收藏的文件记录
 */
data class FavoriteFile(
    val fileName: String,       // 文件名
    val uriString: String,      // URI 字符串
    val fileSize: Long,         // 文件大小（字节）
    val favoriteTime: Long,     // 收藏时间（时间戳）
    val source: String = ""     // 来源（如"来自微信"、"来自分享"、"来自剪贴板"）
) {
    val uri: Uri
        get() = Uri.parse(uriString)

    /**
     * 格式化文件大小显示
     */
    fun getFormattedSize(): String = fileSize.formatFileSize()

    /**
     * 转换为 JSON 字符串
     */
    fun toJson(): String {
        return JSONObject().apply {
            put("fileName", fileName)
            put("uriString", uriString)
            put("fileSize", fileSize)
            put("favoriteTime", favoriteTime)
            put("source", source)
        }.toString()
    }

    companion object {
        /**
         * 从 JSON 字符串解析
         */
        fun fromJson(json: String): FavoriteFile? {
            return try {
                val obj = JSONObject(json)
                FavoriteFile(
                    fileName = obj.getString("fileName"),
                    uriString = obj.getString("uriString"),
                    fileSize = obj.getLong("fileSize"),
                    favoriteTime = obj.getLong("favoriteTime"),
                    source = if (obj.has("source")) obj.getString("source") else ""
                )
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 从 RecentFile 创建
         */
        fun fromRecentFile(recentFile: RecentFile): FavoriteFile {
            return FavoriteFile(
                fileName = recentFile.fileName,
                uriString = recentFile.uriString,
                fileSize = recentFile.fileSize,
                favoriteTime = System.currentTimeMillis(),
                source = recentFile.source
            )
        }
    }
}
