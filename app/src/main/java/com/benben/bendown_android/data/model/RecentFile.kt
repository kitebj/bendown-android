package com.benben.bendown_android.data.model

import android.net.Uri
import com.benben.bendown_android.data.formatFileSize
import org.json.JSONObject

/**
 * 最近打开的文件记录
 */
data class RecentFile(
    val fileName: String,       // 文件名
    val uriString: String,      // URI 字符串
    val fileSize: Long,         // 文件大小（字节）
    val lastOpenedTime: Long,   // 最后打开时间（时间戳）
    val scrollPosition: Int = 0 // 阅读位置（百分比 0-100）
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
            put("lastOpenedTime", lastOpenedTime)
            put("scrollPosition", scrollPosition)
        }.toString()
    }

    companion object {
        /**
         * 从 JSON 字符串解析
         */
        fun fromJson(json: String): RecentFile? {
            return try {
                val obj = JSONObject(json)
                RecentFile(
                    fileName = obj.getString("fileName"),
                    uriString = obj.getString("uriString"),
                    fileSize = obj.getLong("fileSize"),
                    lastOpenedTime = obj.getLong("lastOpenedTime"),
                    scrollPosition = if (obj.has("scrollPosition")) obj.getInt("scrollPosition") else 0
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
