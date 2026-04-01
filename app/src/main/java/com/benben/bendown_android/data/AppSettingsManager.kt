package com.benben.bendown_android.data

import android.content.Context
import android.content.SharedPreferences

/**
 * App 设置管理类
 */
class AppSettingsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_CLIPBOARD_MONITOR_ENABLED = "clipboard_monitor_enabled"

        // 默认值
        private const val DEFAULT_CLIPBOARD_MONITOR_ENABLED = true
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 剪贴板监控是否启用
     */
    var isClipboardMonitorEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLIPBOARD_MONITOR_ENABLED, DEFAULT_CLIPBOARD_MONITOR_ENABLED)
        set(value) {
            prefs.edit().putBoolean(KEY_CLIPBOARD_MONITOR_ENABLED, value).apply()
        }
}
