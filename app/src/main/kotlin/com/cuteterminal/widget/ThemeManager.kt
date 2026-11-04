package com.cuteterminal.widget

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build

object ThemeManager {

    private const val PREFS_NAME = "mochi_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(activity: Activity, mode: String) {
        getPrefs(activity).edit().putString(KEY_THEME_MODE, mode).apply()
        applyTheme(activity, mode)
    }

    fun applyTheme(activity: Activity, mode: String? = null) {
        val selectedMode = mode ?: getThemeMode(activity)
        val targetNightMode = when (selectedMode) {
            THEME_LIGHT -> UiModeManager.MODE_NIGHT_NO
            THEME_DARK -> UiModeManager.MODE_NIGHT_YES
            else -> UiModeManager.MODE_NIGHT_AUTO
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = activity.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            uiModeManager?.setApplicationNightMode(targetNightMode)
        }

        // Synchronously update activity resources configuration so recreate() instantly picks up new theme
        val config = Configuration(activity.resources.configuration)
        val newUiMode = when (selectedMode) {
            THEME_LIGHT -> Configuration.UI_MODE_NIGHT_NO
            THEME_DARK -> Configuration.UI_MODE_NIGHT_YES
            else -> {
                // In system mode, follow system uiMode
                activity.applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            }
        }
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or newUiMode
        @Suppress("DEPRECATION")
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    fun isDarkMode(context: Context): Boolean {
        return when (getThemeMode(context)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> {
                val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}
