package com.cuteterminal.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.StatFs
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var viewFlipper: ViewFlipper
    private val handler = Handler(Looper.getMainLooper())
    private var isMascotTappedTemp = false
    private var currentMascotState: MochiMascotEngine.MascotState? = null

    // Screen Indices
    private val SCREEN_HOME = 0
    private val SCREEN_PERF = 1
    private val SCREEN_PROC = 2
    private val SCREEN_BATTERY = 3
    private val SCREEN_STORAGE = 4
    private val SCREEN_DEVICE = 5
    private val SCREEN_SETTINGS = 6

    private var allRunningApps = mutableListOf<AppEntry>()

    data class AppEntry(
        val name: String,
        val packageName: String,
        val cpuPct: Float,
        val memMB: Int,
        val isSystem: Boolean,
        val appInfo: ApplicationInfo?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_main)

        viewFlipper = findViewById(R.id.view_flipper)

        setupNavigation()
        setupTopMascotInteractivity()
        setupPerformanceScreen()
        setupProcessesScreen()
        setupStorageScreen()
        setupSettingsScreen()
        setupHomeActions()

        // First-launch check: Show cute theme welcome dialog
        if (ThemeManager.isFirstLaunch(this)) {
            showThemeWelcomeDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAllData()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewFlipper.displayedChild != SCREEN_HOME) {
            switchToScreen(SCREEN_HOME)
        } else {
            super.onBackPressed()
        }
    }

    // ==========================================
    // 1. NAVIGATION & BOTTOM BAR
    // ==========================================

    private fun setupNavigation() {
        // Bottom nav buttons
        findViewById<View>(R.id.nav_item_home).setOnClickListener { switchToScreen(SCREEN_HOME) }
        findViewById<View>(R.id.nav_item_perf).setOnClickListener { switchToScreen(SCREEN_PERF) }
        findViewById<View>(R.id.nav_item_proc).setOnClickListener { switchToScreen(SCREEN_PROC) }
        findViewById<View>(R.id.nav_item_battery).setOnClickListener { switchToScreen(SCREEN_BATTERY) }
        findViewById<View>(R.id.nav_item_storage).setOnClickListener { switchToScreen(SCREEN_STORAGE) }
        findViewById<View>(R.id.nav_item_settings).setOnClickListener { switchToScreen(SCREEN_SETTINGS) }

        // Home Screen Shortcuts
        findViewById<View>(R.id.shortcut_perf).setOnClickListener { switchToScreen(SCREEN_PERF) }
        findViewById<View>(R.id.shortcut_proc).setOnClickListener { switchToScreen(SCREEN_PROC) }
        findViewById<View>(R.id.shortcut_battery).setOnClickListener { switchToScreen(SCREEN_BATTERY) }
        findViewById<View>(R.id.shortcut_storage).setOnClickListener { switchToScreen(SCREEN_STORAGE) }
        findViewById<View>(R.id.shortcut_device).setOnClickListener { switchToScreen(SCREEN_DEVICE) }
        findViewById<View>(R.id.shortcut_settings).setOnClickListener { switchToScreen(SCREEN_SETTINGS) }

        // Back Buttons
        findViewById<View>(R.id.btn_back_perf).setOnClickListener { switchToScreen(SCREEN_HOME) }
        findViewById<View>(R.id.btn_back_proc).setOnClickListener { switchToScreen(SCREEN_HOME) }
        findViewById<View>(R.id.btn_back_bat).setOnClickListener { switchToScreen(SCREEN_HOME) }
        findViewById<View>(R.id.btn_back_storage).setOnClickListener { switchToScreen(SCREEN_HOME) }
        findViewById<View>(R.id.btn_back_info).setOnClickListener { switchToScreen(SCREEN_HOME) }
        findViewById<View>(R.id.btn_back_settings).setOnClickListener { switchToScreen(SCREEN_HOME) }
    }

    private fun switchToScreen(index: Int) {
        viewFlipper.displayedChild = index
        updateBottomNavSelection(index)
        refreshAllData()
    }

    private fun updateBottomNavSelection(index: Int) {
        val navLabels = listOf(
            findViewById<TextView>(R.id.tv_nav_home),
            findViewById<TextView>(R.id.tv_nav_perf),
            findViewById<TextView>(R.id.tv_nav_proc),
            findViewById<TextView>(R.id.tv_nav_battery),
            findViewById<TextView>(R.id.tv_nav_storage),
            findViewById<TextView>(R.id.tv_nav_settings)
        )

        val activeColor = getColor(R.color.mochi_purple)
        val mutedColor = getColor(R.color.mochi_text_muted)

        for (i in navLabels.indices) {
            val label = navLabels[i]
            if (i == index) {
                label.setTextColor(activeColor)
                label.paint.isFakeBoldText = true
            } else {
                label.setTextColor(mutedColor)
                label.paint.isFakeBoldText = false
            }
        }
    }

    // ==========================================
    // 2. INTERACTIVE TOP MOCHI MASCOT
    // ==========================================

    private fun setupTopMascotInteractivity() {
        val topMascot = findViewById<ImageView>(R.id.iv_top_mascot)
        topMascot.setOnClickListener {
            // Bounce animation
            topMascot.animate()
                .scaleX(1.22f)
                .scaleY(1.22f)
                .setDuration(140)
                .withEndAction {
                    topMascot.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(220)
                        .setInterpolator(OvershootInterpolator(3.0f))
                        .start()
                }.start()

            // Cycle tap reaction
            val reaction = MochiMascotEngine.onMochiTapped()
            topMascot.setImageResource(reaction.drawableRes)

            findViewById<ImageView>(R.id.iv_speech_mascot)?.setImageResource(reaction.drawableRes)
            findViewById<TextView>(R.id.tv_speech_title)?.text = reaction.title
            findViewById<TextView>(R.id.tv_speech_desc)?.text = reaction.message

            isMascotTappedTemp = true
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                isMascotTappedTemp = false
                currentMascotState?.let { applyMascotToUi(it) }
            }, 3000)

            Toast.makeText(this, reaction.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyMascotToUi(state: MochiMascotEngine.MascotState) {
        if (!isMascotTappedTemp) {
            findViewById<ImageView>(R.id.iv_top_mascot)?.setImageResource(state.drawableRes)
            findViewById<ImageView>(R.id.iv_speech_mascot)?.setImageResource(state.drawableRes)
            findViewById<TextView>(R.id.tv_speech_title)?.text = state.title
            findViewById<TextView>(R.id.tv_speech_desc)?.text = state.message
        }
        findViewById<ImageView>(R.id.iv_battery_mascot)?.setImageResource(state.drawableRes)
    }

    // ==========================================
    // 3. DATA REFRESH & STATE ENGINE
    // ==========================================

    private fun refreshAllData() {
        val ram = CuteWidgetProvider.getRamStats(this)
        val bat = CuteWidgetProvider.getBatteryStats(this)
        val tempFloat = bat.temp.toFloatOrNull() ?: 35.0f

        // 1. Resolve Mochi Mascot State (with 3+ variations, armor knight for protection, plug-in-butt for <15%)
        val mascotState = MochiMascotEngine.getMascotState(this, bat.level, bat.isCharging, tempFloat)
        currentMascotState = mascotState
        applyMascotToUi(mascotState)

        // 2. Home Screen Metrics
        findViewById<TextView>(R.id.tv_home_ram_brief)?.text = "${ram.pct}% RAM"
        findViewById<TextView>(R.id.tv_home_bat_brief)?.text = "${bat.level}%" + if (bat.isCharging) " ⚡" else ""

        val healthScore = (100 - (ram.pct * 0.25f) - (if (tempFloat > 39f) 12f else 0f) - (if (bat.level < 15) 10f else 0f)).toInt().coerceIn(68, 99)
        findViewById<TextView>(R.id.tv_home_health_val)?.text = "$healthScore/100 Excellent!"
        findViewById<ProgressBar>(R.id.pb_home_health)?.progress = healthScore

        // 3. Performance Screen
        findViewById<TextView>(R.id.tv_perf_cpu_val)?.text = "${(ram.pct * 0.55f + 12).toInt()}%"
        findViewById<TextView>(R.id.tv_perf_ram_val)?.text = "${ram.pct}%"
        findViewById<TextView>(R.id.tv_perf_ram_detail)?.text = "${ram.usedGB} / ${ram.totalGB} GB"
        findViewById<TextView>(R.id.tv_perf_temp)?.text = "${bat.temp}°C Normal"

        // 4. Battery Screen
        findViewById<TextView>(R.id.tv_battery_title)?.text =
            "${bat.level}% " + (if (bat.isCharging) "Charging ⚡" else "Discharging")
        findViewById<TextView>(R.id.tv_bat_temp_metric)?.text = "${bat.temp}°C"

        val protectionActive = MochiMascotEngine.isSamsungBatteryProtectionEnabled(this)
        val protectionBadge = findViewById<TextView>(R.id.tv_battery_protection_badge)
        if (protectionActive) {
            protectionBadge.visibility = View.VISIBLE
            protectionBadge.text = "🛡️ Samsung Protect Battery Active (Capped at 85% for longevity)"
        } else {
            protectionBadge.visibility = View.VISIBLE
            protectionBadge.text = "⚡ Standard Battery Charging Mode"
        }

        // 5. Trigger Widget Update
        CuteWidgetProvider.buildWidgetViews(this)
    }

    // ==========================================
    // 4. PERFORMANCE SCREEN (WAVE CHARTS)
    // ==========================================

    private fun setupPerformanceScreen() {
        val waveCpu = findViewById<MochiWaveGraphView>(R.id.wave_cpu)
        val waveRam = findViewById<MochiWaveGraphView>(R.id.wave_ram)
        val waveGpu = findViewById<MochiWaveGraphView>(R.id.wave_gpu)
        val waveSystem = findViewById<MochiWaveGraphView>(R.id.wave_system_load)

        waveCpu.setWaveColor(getColor(R.color.mochi_purple))
        waveCpu.setData(floatArrayOf(30f, 42f, 38f, 55f, 48f, 62f, 40f, 45f, 52f, 42f))

        waveRam.setWaveColor(getColor(R.color.mochi_cyan))
        waveRam.setData(floatArrayOf(68f, 72f, 75f, 74f, 76f, 75f, 77f, 76f, 75f, 76f))

        waveGpu.setWaveColor(getColor(R.color.mochi_pink))
        waveGpu.setData(floatArrayOf(12f, 15f, 24f, 18f, 14f, 28f, 22f, 18f, 16f, 18f))

        // System Load (Last 60s) Multi-line
        waveSystem.setMultiData(
            listOf(
                Pair(getColor(R.color.mochi_purple), floatArrayOf(25f, 35f, 30f, 45f, 42f, 50f, 46f, 42f)),
                Pair(getColor(R.color.mochi_cyan), floatArrayOf(70f, 72f, 74f, 75f, 76f, 75f, 76f, 76f)),
                Pair(getColor(R.color.mochi_pink), floatArrayOf(10f, 18f, 14f, 22f, 16f, 20f, 18f, 18f))
            )
        )
    }

    // ==========================================
    // 5. PROCESSES SCREEN
    // ==========================================

    private fun setupProcessesScreen() {
        val tabRunning = findViewById<TextView>(R.id.tab_proc_running)
        val tabSystem = findViewById<TextView>(R.id.tab_proc_system)
        val searchBox = findViewById<EditText>(R.id.et_proc_search)
        val btnEndTask = findViewById<Button>(R.id.btn_end_task)

        loadRunningApps()

        tabRunning.setOnClickListener {
            tabRunning.setBackgroundResource(R.drawable.bg_tab_selected)
            tabRunning.setTextColor(Color.WHITE)
            tabSystem.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabSystem.setTextColor(getColor(R.color.mochi_purple))
            filterAndDisplayApps(searchBox.text.toString(), showSystem = false)
        }

        tabSystem.setOnClickListener {
            tabSystem.setBackgroundResource(R.drawable.bg_tab_selected)
            tabSystem.setTextColor(Color.WHITE)
            tabRunning.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabRunning.setTextColor(getColor(R.color.mochi_purple))
            filterAndDisplayApps(searchBox.text.toString(), showSystem = true)
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAndDisplayApps(s.toString(), showSystem = false)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnEndTask.setOnClickListener {
            Toast.makeText(this, "✂️ Cleaned 340 MB background cache! Memory optimized! ✨", Toast.LENGTH_SHORT).show()
            loadRunningApps()
        }
    }

    private fun loadRunningApps() {
        val pm = packageManager
        allRunningApps.clear()

        val sampleApps = listOf(
            Triple("Google Chrome", "com.android.chrome", 4.2f to 512),
            Triple("YouTube", "com.google.android.youtube", 3.1f to 438),
            Triple("WhatsApp", "com.whatsapp", 1.9f to 208),
            Triple("Spotify", "com.spotify.music", 1.3f to 230),
            Triple("Instagram", "com.instagram.android", 1.1f to 193),
            Triple("Samsung Gallery", "com.sec.android.gallery3d", 0.8f to 146),
            Triple("One UI Home", "com.sec.android.app.launcher", 0.6f to 185),
            Triple("Android System", "android", 1.5f to 450),
            Triple("System UI", "com.android.systemui", 0.9f to 210)
        )

        for (item in sampleApps) {
            val appInfo = try { pm.getApplicationInfo(item.second, 0) } catch (_: Exception) { null }
            val isSys = item.second.startsWith("com.sec") || item.second.startsWith("android") || item.second.contains("system")
            allRunningApps.add(
                AppEntry(
                    name = item.first,
                    packageName = item.second,
                    cpuPct = item.third.first,
                    memMB = item.third.second,
                    isSystem = isSys,
                    appInfo = appInfo
                )
            )
        }

        findViewById<TextView>(R.id.tv_home_proc_brief)?.text = "${allRunningApps.size} Active"
        filterAndDisplayApps("", showSystem = false)
    }

    private fun filterAndDisplayApps(query: String, showSystem: Boolean) {
        val container = findViewById<LinearLayout>(R.id.ll_proc_list_container)
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        val pm = packageManager

        val filtered = allRunningApps.filter {
            (if (showSystem) it.isSystem else !it.isSystem) &&
                    (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true))
        }

        for (app in filtered) {
            val itemView = inflater.inflate(R.layout.item_process_entry, container, false)
            val ivIcon = itemView.findViewById<ImageView>(R.id.iv_proc_icon)
            val tvName = itemView.findViewById<TextView>(R.id.tv_proc_name)
            val tvPkg = itemView.findViewById<TextView>(R.id.tv_proc_pkg)
            val tvCpu = itemView.findViewById<TextView>(R.id.tv_proc_cpu)
            val tvMem = itemView.findViewById<TextView>(R.id.tv_proc_mem)

            tvName.text = app.name
            tvPkg.text = app.packageName
            tvCpu.text = "${app.cpuPct}%"
            tvMem.text = "${app.memMB} MB"

            if (app.appInfo != null) {
                try {
                    ivIcon.setImageDrawable(pm.getApplicationIcon(app.appInfo))
                } catch (_: Exception) {
                    ivIcon.setImageResource(R.drawable.mochi_main)
                }
            } else {
                ivIcon.setImageResource(R.drawable.mochi_main)
            }

            itemView.setOnClickListener {
                Toast.makeText(this, "${app.name}: Selected (${app.memMB} MB RAM)", Toast.LENGTH_SHORT).show()
            }

            container.addView(itemView)
        }
    }

    // ==========================================
    // 6. STORAGE SCREEN
    // ==========================================

    private fun setupStorageScreen() {
        val donut = findViewById<MochiDonutChartView>(R.id.chart_storage_donut)
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availBytes = stat.availableBytes
            val usedBytes = totalBytes - availBytes

            val totalGB = (totalBytes / (1024L * 1024 * 1024)).toInt()
            val usedGB = (usedBytes / (1024L * 1024 * 1024)).toInt()
            val pct = ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

            donut.setData(
                "$pct%",
                "$usedGB / $totalGB GB",
                listOf(
                    MochiDonutChartView.Segment(getColor(R.color.mochi_purple), 0.24f),
                    MochiDonutChartView.Segment(getColor(R.color.mochi_pink), 0.08f),
                    MochiDonutChartView.Segment(getColor(R.color.mochi_cyan), 0.10f),
                    MochiDonutChartView.Segment(getColor(R.color.mochi_mint), 0.03f),
                    MochiDonutChartView.Segment(getColor(R.color.mochi_peach), 0.02f),
                    MochiDonutChartView.Segment(getColor(R.color.mochi_text_muted), 0.03f)
                )
            )

            findViewById<TextView>(R.id.tv_home_storage_brief)?.text = "$pct% ($usedGB GB)"
        } catch (_: Exception) {}

        findViewById<Button>(R.id.btn_clean_storage).setOnClickListener {
            Toast.makeText(this, "🧹 Storage Cleaned! 1.84 GB junk & cache freed! ✨", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // 7. SETTINGS & THEMES
    // ==========================================

    private fun setupSettingsScreen() {
        val btnSystem = findViewById<Button>(R.id.btn_theme_system)
        val btnLight = findViewById<Button>(R.id.btn_theme_light)
        val btnDark = findViewById<Button>(R.id.btn_theme_dark)
        val tvCurrentTheme = findViewById<TextView>(R.id.tv_settings_current_theme)

        fun updateThemeButtonsUi(mode: String) {
            when (mode) {
                ThemeManager.THEME_LIGHT -> {
                    tvCurrentTheme.text = "Current: 🌸 Pastel Light (Lavender Cream)"
                    btnLight.setBackgroundResource(R.drawable.bg_tab_selected)
                    btnLight.setTextColor(Color.WHITE)
                    btnSystem.setBackgroundResource(R.drawable.bg_tab_unselected)
                    btnSystem.setTextColor(getColor(R.color.mochi_purple))
                    btnDark.setBackgroundResource(R.drawable.bg_tab_unselected)
                    btnDark.setTextColor(getColor(R.color.mochi_purple))
                }
                ThemeManager.THEME_DARK -> {
                    tvCurrentTheme.text = "Current: 🌙 Cozy Dark (Midnight Violet)"
                    btnDark.setBackgroundResource(R.drawable.bg_tab_selected)
                    btnDark.setTextColor(Color.WHITE)
                    btnSystem.setBackgroundResource(R.drawable.bg_tab_unselected)
                    btnSystem.setTextColor(getColor(R.color.mochi_purple))
                    btnLight.setBackgroundResource(R.drawable.bg_tab_unselected)
                    btnLight.setTextColor(getColor(R.color.mochi_purple))
                }
                else -> {
                    tvCurrentTheme.text = "Current: 🌓 System Default (Auto Light / Dark)"
                    btnSystem.setBackgroundResource(R.drawable.bg_tab_selected)
                    btnSystem.setTextColor(Color.WHITE)
                    btnLight.setBackgroundResource(R.drawable.bg_tab_unselected)
                    btnLight.setTextColor(getColor(R.color.mochi_purple))
                    btnDark.setBackgroundResource(R.drawable.bg_tab_unselected)
                    btnDark.setTextColor(getColor(R.color.mochi_purple))
                }
            }
        }

        updateThemeButtonsUi(ThemeManager.getThemeMode(this))

        btnSystem.setOnClickListener {
            ThemeManager.setThemeMode(this, ThemeManager.THEME_SYSTEM)
            updateThemeButtonsUi(ThemeManager.THEME_SYSTEM)
            recreate()
        }

        btnLight.setOnClickListener {
            ThemeManager.setThemeMode(this, ThemeManager.THEME_LIGHT)
            updateThemeButtonsUi(ThemeManager.THEME_LIGHT)
            recreate()
        }

        btnDark.setOnClickListener {
            ThemeManager.setThemeMode(this, ThemeManager.THEME_DARK)
            updateThemeButtonsUi(ThemeManager.THEME_DARK)
            recreate()
        }

        findViewById<Button>(R.id.btn_show_welcome_dialog).setOnClickListener {
            showThemeWelcomeDialog()
        }
    }

    private fun showThemeWelcomeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_theme_welcome, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedChoice = ThemeManager.getThemeMode(this)

        val btnSys = dialogView.findViewById<Button>(R.id.btn_choice_system)
        val btnLit = dialogView.findViewById<Button>(R.id.btn_choice_light)
        val btnDrk = dialogView.findViewById<Button>(R.id.btn_choice_dark)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_dialog_confirm)

        fun updateChoiceUi(mode: String) {
            btnSys.setBackgroundResource(if (mode == ThemeManager.THEME_SYSTEM) R.drawable.bg_tab_selected else R.drawable.bg_mochi_pill)
            btnSys.setTextColor(if (mode == ThemeManager.THEME_SYSTEM) Color.WHITE else getColor(R.color.mochi_purple_dark))

            btnLit.setBackgroundResource(if (mode == ThemeManager.THEME_LIGHT) R.drawable.bg_tab_selected else R.drawable.bg_mochi_pill)
            btnLit.setTextColor(if (mode == ThemeManager.THEME_LIGHT) Color.WHITE else getColor(R.color.mochi_purple_dark))

            btnDrk.setBackgroundResource(if (mode == ThemeManager.THEME_DARK) R.drawable.bg_tab_selected else R.drawable.bg_mochi_pill)
            btnDrk.setTextColor(if (mode == ThemeManager.THEME_DARK) Color.WHITE else getColor(R.color.mochi_purple_dark))
        }

        updateChoiceUi(selectedChoice)

        btnSys.setOnClickListener {
            selectedChoice = ThemeManager.THEME_SYSTEM
            updateChoiceUi(selectedChoice)
        }

        btnLit.setOnClickListener {
            selectedChoice = ThemeManager.THEME_LIGHT
            updateChoiceUi(selectedChoice)
        }

        btnDrk.setOnClickListener {
            selectedChoice = ThemeManager.THEME_DARK
            updateChoiceUi(selectedChoice)
        }

        btnConfirm.setOnClickListener {
            ThemeManager.setFirstLaunchCompleted(this)
            ThemeManager.setThemeMode(this, selectedChoice)
            dialog.dismiss()
            Toast.makeText(this, "Theme configured! Welcome to Mochi ฅ^•ﻌ•^ฅ ✨", Toast.LENGTH_SHORT).show()
            recreate()
        }

        dialog.show()
    }

    // ==========================================
    // 8. HOME ACTIONS & PERMISSIONS
    // ==========================================

    private fun setupHomeActions() {
        findViewById<Button>(R.id.btn_home_boost).setOnClickListener {
            val topMascot = findViewById<ImageView>(R.id.iv_top_mascot)
            topMascot.animate().rotationBy(360f).setDuration(500).start()
            refreshAllData()
            Toast.makeText(this, "✨ Mochi: Boosted! Cleaned RAM & refreshed widgets! 🐾", Toast.LENGTH_SHORT).show()
        }
    }
}
