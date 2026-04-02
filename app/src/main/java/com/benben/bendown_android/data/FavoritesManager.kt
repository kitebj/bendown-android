package com.benben.bendown_android.data

import android.content.Context
import android.content.SharedPreferences
import com.benben.bendown_android.data.model.FavoriteFile
import com.benben.bendown_android.data.model.RecentFile

/**
 * 收藏管理类
 */
class FavoritesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "favorites"
        private const val KEY_FAVORITES = "favorites_list"
        private const val MAX_FAVORITES = 100 // 最多收藏100个文件
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 获取所有收藏（按收藏时间倒序）
     */
    fun getAll(): List<FavoriteFile> {
        val jsonString = prefs.getString(KEY_FAVORITES, "[]") ?: "[]"
        return try {
            val list = mutableListOf<FavoriteFile>()
            val jsonArray = org.json.JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                FavoriteFile.fromJson(jsonArray.getString(i))?.let { list.add(it) }
            }
            // 按收藏时间倒序
            list.sortedByDescending { it.favoriteTime }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 检查是否已收藏
     */
    fun isFavorite(uriString: String): Boolean {
        return getAll().any { it.uriString == uriString }
    }

    /**
     * 添加收藏
     */
    fun add(file: FavoriteFile) {
        val list = getAll().toMutableList()

        // 如果已存在，先移除旧的
        list.removeAll { it.uriString == file.uriString }

        // 添加到开头（最新的在最前）
        list.add(0, file)

        // 限制数量
        if (list.size > MAX_FAVORITES) {
            list.removeAt(list.size - 1)
        }

        save(list)
    }

    /**
     * 从 RecentFile 添加收藏
     */
    fun addFromRecent(recentFile: RecentFile) {
        add(FavoriteFile.fromRecentFile(recentFile))
    }

    /**
     * 取消收藏
     */
    fun remove(uriString: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.uriString == uriString }
        save(list)
    }

    /**
     * 清空所有收藏
     */
    fun clear() {
        save(emptyList())
    }

    /**
     * 获取收藏数量
     */
    fun getCount(): Int = getAll().size

    /**
     * 保存到 SharedPreferences
     */
    private fun save(list: List<FavoriteFile>) {
        val jsonArray = org.json.JSONArray()
        list.forEach { jsonArray.put(it.toJson()) }
        prefs.edit().putString(KEY_FAVORITES, jsonArray.toString()).apply()
    }
}
