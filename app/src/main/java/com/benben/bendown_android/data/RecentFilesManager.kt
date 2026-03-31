package com.benben.bendown_android.data

import android.content.Context
import android.content.SharedPreferences
import com.benben.bendown_android.data.model.RecentFile
import org.json.JSONArray

/**
 * 最近文件记录管理器
 */
class RecentFilesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "recent_files_prefs"
        private const val KEY_RECENT_FILES = "recent_files"
        private const val MAX_RECORDS = 100    // 最多存储100条
        private const val MAX_DISPLAY = 20     // 显示最新20条
    }

    /**
     * 获取显示用的最近文件记录（最新20条）
     */
    fun getDisplayList(): List<RecentFile> {
        return getAll().take(MAX_DISPLAY)
    }

    /**
     * 获取所有最近文件记录
     */
    fun getAll(): List<RecentFile> {
        val json = prefs.getString(KEY_RECENT_FILES, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<RecentFile>()

        for (i in 0 until array.length()) {
            RecentFile.fromJson(array.getString(i))?.let { list.add(it) }
        }

        // 按最后打开时间倒序排列
        return list.sortedByDescending { it.lastOpenedTime }
    }

    /**
     * 根据URI获取记录
     */
    fun getByUri(uriString: String): RecentFile? {
        return getAll().find { it.uriString == uriString }
    }

    /**
     * 添加或更新一条记录
     */
    fun add(record: RecentFile) {
        val list = getAll().toMutableList()

        // 如果已存在相同 URI，先移除旧的
        list.removeAll { it.uriString == record.uriString }

        // 添加新记录到开头
        list.add(0, record)

        // 只保留最近 100 条
        val trimmed = list.take(MAX_RECORDS)

        save(trimmed)
    }

    /**
     * 更新阅读位置
     */
    fun updateScrollPosition(uriString: String, scrollPosition: Int) {
        val list = getAll().toMutableList()
        val index = list.indexOfFirst { it.uriString == uriString }
        
        if (index >= 0) {
            val old = list[index]
            list[index] = old.copy(scrollPosition = scrollPosition)
            save(list)
        }
    }

    /**
     * 移除一条记录
     */
    fun remove(uriString: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.uriString == uriString }
        save(list)
    }

    /**
     * 清空所有记录
     */
    fun clear() {
        prefs.edit().remove(KEY_RECENT_FILES).apply()
    }

    /**
     * 保存记录列表
     */
    private fun save(list: List<RecentFile>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_RECENT_FILES, array.toString()).apply()
    }
}
