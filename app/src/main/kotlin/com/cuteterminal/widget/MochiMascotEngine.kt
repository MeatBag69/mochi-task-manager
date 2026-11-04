package com.cuteterminal.widget

import android.content.Context
import android.provider.Settings
import java.util.Random

object MochiMascotEngine {

    private val random = Random()
    private var tapIndex = 0

    // Tap interactive reactions
    private val tapSprites = listOf(
        R.drawable.mochi_main,
        R.drawable.mochi_tap_2,
        R.drawable.mochi_tap_3,
        R.drawable.mochi_tap_4
    )

    private val tapPhrases = listOf(
        "ฅ^•ﻌ•^ฅ *boop!* You poked Mochi!",
        "(✿◠‿◠) Hehe, that tickles! Systems running at peak speed!",
        "✨ System Boosted! All background bloat kept in check!",
        "🛡️ Knight Mochi is guarding your Galaxy S21 FE 24/7!"
    )

    // Battery stages - At least 3 variations each
    val critSprites = listOf(
        R.drawable.mochi_bat_crit_1,
        R.drawable.mochi_bat_crit_2,
        R.drawable.mochi_bat_crit_3
    )

    val lowSprites = listOf(
        R.drawable.mochi_bat_low_1,
        R.drawable.mochi_bat_low_2,
        R.drawable.mochi_bat_low_3
    )

    val midSprites = listOf(
        R.drawable.mochi_bat_mid_1,
        R.drawable.mochi_bat_mid_2,
        R.drawable.mochi_bat_mid_3
    )

    val goodSprites = listOf(
        R.drawable.mochi_bat_good_1,
        R.drawable.mochi_bat_good_2,
        R.drawable.mochi_bat_good_3
    )

    val fullSprites = listOf(
        R.drawable.mochi_bat_full_1,
        R.drawable.mochi_bat_full_2,
        R.drawable.mochi_bat_full_3
    )

    // Battery protection mode: Knight with armor & shield/spear
    val protectSprites = listOf(
        R.drawable.mochi_knight_shield,
        R.drawable.mochi_protect_1,
        R.drawable.mochi_protect_2
    )

    // Active charging
    val chargeSprites = listOf(
        R.drawable.mochi_charge_1,
        R.drawable.mochi_charge_2,
        R.drawable.mochi_charge_3
    )

    // Unplugged / disconnected transitions
    val unplugSprites = listOf(
        R.drawable.mochi_unplug_1,
        R.drawable.mochi_unplug_2,
        R.drawable.mochi_unplug_3
    )

    data class MascotState(
        val drawableRes: Int,
        val title: String,
        val message: String,
        val isProtected: Boolean = false,
        val isCritical: Boolean = false
    )

    /**
     * Checks if Samsung One UI Protect Battery is enabled.
     * Setting key: "protect_battery" (1 = on, 0 = off).
     */
    fun isSamsungBatteryProtectionEnabled(context: Context): Boolean {
        return try {
            val settingVal = Settings.Global.getInt(context.contentResolver, "protect_battery", 0)
            settingVal != 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves the current dynamic Mascot state based on battery, charging, and protection mode.
     */
    fun getMascotState(
        context: Context,
        level: Int,
        isCharging: Boolean,
        tempCelsius: Float,
        unpluggedRecently: Boolean = false
    ): MascotState {
        val protectionActive = isSamsungBatteryProtectionEnabled(context)

        // 1. Unplugged transition
        if (unpluggedRecently) {
            val sprite = unplugSprites[random.nextInt(unplugSprites.size)]
            return MascotState(
                drawableRes = sprite,
                title = "Charger Unplugged!",
                message = "🔌 Charger removed! Mochi switched to active mobile battery mode."
            )
        }

        // 2. Battery Protection Mode (Samsung Protect Battery enabled & level around 80-85% or plugged in)
        if (protectionActive && (level in 78..86 || isCharging)) {
            val sprite = protectSprites[random.nextInt(protectSprites.size)]
            return MascotState(
                drawableRes = sprite,
                title = "Battery Protection Active! 🛡️",
                message = "⚔️ Knight Mochi wearing armor with shield & spear guarding your battery lifespan! (Capped at 80-85%)",
                isProtected = true
            )
        }

        // 3. Normal Charging
        if (isCharging) {
            val sprite = chargeSprites[random.nextInt(chargeSprites.size)]
            return MascotState(
                drawableRes = sprite,
                title = "Powering Up! ⚡",
                message = "⚡ Charging at $level% · Healthy battery temperature (${tempCelsius}°C)."
            )
        }

        // 4. Overheating alert
        if (tempCelsius > 39.5f) {
            return MascotState(
                drawableRes = R.drawable.mochi_cooling,
                title = "Cooling Down! ❄️",
                message = "🌡️ Phone is warm (${tempCelsius}°C)! Mochi is running cooling protocols."
            )
        }

        // 5. Battery Stages
        return when {
            level < 15 -> {
                val sprite = critSprites[random.nextInt(critSprites.size)]
                MascotState(
                    drawableRes = sprite,
                    title = "Battery Critical! (< 15%)",
                    message = "🔌 Plug in butt mode! Battery is at $level%! Please plug in charger immediately!",
                    isCritical = true
                )
            }
            level in 15..24 -> {
                val sprite = lowSprites[random.nextInt(lowSprites.size)]
                MascotState(
                    drawableRes = sprite,
                    title = "Low Battery (15% - 25%)",
                    message = "🔋 Battery is at $level% · Mochi is getting sleepy, plug in soon!"
                )
            }
            level in 25..54 -> {
                val sprite = midSprites[random.nextInt(midSprites.size)]
                MascotState(
                    drawableRes = sprite,
                    title = "Working Hard (25% - 55%)",
                    message = "💻 Battery at $level% · Mochi is actively monitoring background processes!"
                )
            }
            level in 55..84 -> {
                val sprite = goodSprites[random.nextInt(goodSprites.size)]
                MascotState(
                    drawableRes = sprite,
                    title = "Good & Happy (55% - 85%)",
                    message = "✿ ( ˶•̀ᴗ•́˶) Battery at $level% · Plenty of juice for gaming & chatting!"
                )
            }
            else -> {
                val sprite = fullSprites[random.nextInt(fullSprites.size)]
                MascotState(
                    drawableRes = sprite,
                    title = "Full Power! (85% - 100%)",
                    message = "🦸 Battery at $level%! Full power superhero mode active!"
                )
            }
        }
    }

    /**
     * Cycles to the next interactive tap sprite and phrase.
     */
    fun onMochiTapped(): MascotState {
        val sprite = tapSprites[tapIndex % tapSprites.size]
        val phrase = tapPhrases[tapIndex % tapPhrases.size]
        tapIndex++
        return MascotState(
            drawableRes = sprite,
            title = "Mochi Interaction! ✨",
            message = phrase
        )
    }

    /**
     * Randomly picks a cute mascot for a specific screen so navigating shuffles through characters!
     */
    fun getRandomMascotForScreen(screenIndex: Int, context: Context): Int {
        val protectionActive = isSamsungBatteryProtectionEnabled(context)
        return when (screenIndex) {
            0 -> listOf(R.drawable.mochi_main, R.drawable.mochi_happy, R.drawable.mochi_bat_good_1, R.drawable.mochi_king)[random.nextInt(4)]
            1 -> listOf(R.drawable.mochi_working, R.drawable.mochi_bat_mid_1, R.drawable.mochi_unplug_1)[random.nextInt(3)]
            2 -> listOf(R.drawable.mochi_tap_2, R.drawable.mochi_working, R.drawable.mochi_bat_mid_2)[random.nextInt(3)]
            3 -> if (protectionActive) R.drawable.mochi_knight_shield else listOf(R.drawable.mochi_charge_1, R.drawable.mochi_bat_good_1, R.drawable.mochi_battery_power)[random.nextInt(3)]
            4 -> listOf(R.drawable.mochi_astronaut, R.drawable.mochi_bat_low_2, R.drawable.mochi_tap_3)[random.nextInt(3)]
            5 -> listOf(R.drawable.mochi_doctor, R.drawable.mochi_bat_good_3, R.drawable.mochi_main)[random.nextInt(3)]
            6 -> listOf(R.drawable.mochi_peek, R.drawable.mochi_bat_good_2, R.drawable.mochi_king)[random.nextInt(3)]
            else -> R.drawable.mochi_main
        }
    }
}
