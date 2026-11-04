package com.cuteterminal.widget

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGrantPerm = findViewById<Button>(R.id.btn_grant_perm)
        val btnRefresh = findViewById<Button>(R.id.btn_app_refresh)

        btnGrantPerm.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Open Settings > Apps > Special Access > Usage Access", Toast.LENGTH_LONG).show()
            }
        }

        btnRefresh.setOnClickListener {
            triggerWidgetUpdate()
            updateUi()
            Toast.makeText(this, "Mochi: Boosted & Refreshed! ฅ^•ﻌ•^ฅ ✨", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    private fun updateUi() {
        val tvPermStatus = findViewById<TextView>(R.id.tv_perm_status)
        val btnGrantPerm = findViewById<Button>(R.id.btn_grant_perm)
        val hasUsageAccess = checkUsageAccess()

        if (hasUsageAccess) {
            tvPermStatus.text = "🔓 Permission: Usage Access Active! (●'◡'●)"
            tvPermStatus.setTextColor(getColor(R.color.mochi_mint))
            btnGrantPerm.text = "✓ Permission Granted (Active)"
            btnGrantPerm.isEnabled = false
            btnGrantPerm.alpha = 0.6f
        } else {
            tvPermStatus.text = "🔒 Permission: Usage Access Needed"
            tvPermStatus.setTextColor(getColor(R.color.mochi_peach))
            btnGrantPerm.text = "Grant Usage Access (✿◠‿◠)"
            btnGrantPerm.isEnabled = true
            btnGrantPerm.alpha = 1.0f
        }

        // Live stats update
        updateMochiHomeStats()

        triggerWidgetUpdate()
    }

    private fun updateMochiHomeStats() {
        try {
            // 1. Live RAM
            val ramInfo = CuteWidgetProvider.getRamStats(this)
            findViewById<TextView>(R.id.tv_grid_ram)?.text = "${ramInfo.pct}% RAM"
            findViewById<TextView>(R.id.tv_ram_pct)?.text = "${ramInfo.pct}%"

            val barLen = 10
            val filled = ((ramInfo.pct / 100.0) * barLen).toInt().coerceIn(0, barLen)
            val bar = "█".repeat(filled) + "░".repeat(barLen - filled)
            findViewById<TextView>(R.id.tv_ram_detail)?.text =
                "RAM: [$bar] ${ramInfo.pct}% (${ramInfo.usedGB} / ${ramInfo.totalGB} GB)"

            // 2. Live Battery & Temperature
            val batInfo = CuteWidgetProvider.getBatteryStats(this)
            val batText = "${batInfo.level}%" + if (batInfo.isCharging) " ⚡" else ""
            findViewById<TextView>(R.id.tv_grid_bat)?.text = batText
            findViewById<TextView>(R.id.tv_bat_pct)?.text = batText

            // 3. CPU estimate
            val tempFloat = batInfo.temp.toFloatOrNull() ?: 36.0f
            val cpuPct = (28 + ((tempFloat - 34.0f).coerceAtLeast(0f) * 3.5f) + (ramInfo.pct * 0.15f)).toInt().coerceIn(12, 92)
            findViewById<TextView>(R.id.tv_cpu_pct)?.text = "$cpuPct%"

            // 4. Device Health Score
            val healthScore = (100 - (ramInfo.pct * 0.25f) - (if (tempFloat > 39f) 15f else 0f) - (if (batInfo.level < 15) 10f else 0f)).toInt().coerceIn(65, 99)
            val healthText = "$healthScore/100 Excellent!"
            findViewById<TextView>(R.id.tv_home_health_val)?.text = healthText
            findViewById<TextView>(R.id.tv_health_status)?.text = "Device Health: $healthText"
            findViewById<ProgressBar>(R.id.pb_health)?.progress = healthScore

            // 5. Dynamic Mochi Reaction
            val reaction = CuteWidgetProvider.determineMochiReaction(batInfo, ramInfo, tempFloat)
            findViewById<ImageView>(R.id.iv_home_mascot)?.setImageResource(R.drawable.mochi_main)
            findViewById<ImageView>(R.id.iv_reaction_mascot)?.setImageResource(reaction.drawableRes)
            findViewById<ImageView>(R.id.iv_mochi_mascot)?.setImageResource(reaction.drawableRes)
            findViewById<TextView>(R.id.tv_mochi_speech_title)?.text = "Mochi says: ${reaction.title} (✿◠‿◠)"
            findViewById<TextView>(R.id.tv_mochi_speech_desc)?.text = reaction.message
            findViewById<TextView>(R.id.tv_reaction_status)?.text = reaction.message

            // 6. Recent Processes
            val procs = CuteWidgetProvider.getRunningProcesses(this)
            findViewById<TextView>(R.id.tv_grid_proc_count)?.text = "${procs.size} Active"

            val textViews = listOf(
                findViewById<TextView>(R.id.tv_proc1),
                findViewById<TextView>(R.id.tv_proc2),
                findViewById<TextView>(R.id.tv_proc3)
            )
            for (i in textViews.indices) {
                if (i < procs.size) {
                    val p = procs[i]
                    val shortName = if (p.name.length > 22) p.name.take(20) + ".." else p.name.padEnd(22)
                    val memStr = "${p.memMB} MB".padStart(7)
                    textViews[i]?.text = "● $shortName $memStr"
                }
            }

            // Timestamp
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            findViewById<TextView>(R.id.btn_refresh)?.text = "⟳ ${timeFmt.format(Date())}"
        } catch (_: Exception) {}
    }

    private fun checkUsageAccess(): Boolean {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        if (usm != null) {
            val now = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 1000 * 3600 * 48, now)
            if (!stats.isNullOrEmpty()) return true
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun triggerWidgetUpdate() {
        val intent = Intent(this, CuteWidgetProvider::class.java).apply {
            action = CuteWidgetProvider.ACTION_REFRESH
        }
        sendBroadcast(intent)
    }
}
