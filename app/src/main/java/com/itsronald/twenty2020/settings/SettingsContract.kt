package com.itsronald.twenty2020.settings

import android.support.annotation.ColorInt
import com.itsronald.twenty2020.base.Presenter
import com.itsronald.twenty2020.base.View

interface SettingsContract {

    companion object {
        val KEY_WORK_PHASE_LENGTH = "com.itsronald.settings.key.work_phase_length"

        val KEY_BREAK_PHASE_LENGTH = "com.itsronald.settings.key.break_phase_length"

        val KEY_NIGHT_MODE = "com.itsronald.settings.key.night_mode"

        val KEY_NIGHT_MODE_USES_LOCATION = "com.itsronald.settings.key.night_mode_uses_location"

        val KEY_TIMER_KEEPS_SCREEN_ON = "com.itsronald.settings.key.timer_keeps_screen_on"

        val KEY_TIMER_ALLOWS_FULLSCREEN = "com.itsronald.settings.key.timer_allows_fullscreen"

        val KEY_NOTIFICATION_IS_PERSISTENT = "com.itsronald.settings.key.notification_is_persistent"

        val KEY_NOTIFICATION_SOUND_ENABLED = "com.itsronald.settings.key.notification_sound_enabled"

        val KEY_NOTIFICATION_VIBRATE_ENABLED = "com.itsronald.settings.key.notification_vibrate_enabled"

        val KEY_NOTIFICATION_LED_ENABLED = "com.itsronald.settings.key.notification_led_enabled"

        val KEY_NOTIFICATION_LED_COLOR = "com.itsronald.settings.key.notification_led_color"
    }

    interface SettingsView : View<UserActionsListener> {

    }

    interface UserActionsListener : Presenter<SettingsView> {

        //region General prefs

        fun setWorkPhaseLength(length: Int)

        fun setBreakPhaseLength(length: Int)

        fun startUpgrade()

        //endregion

        //region Display prefs

        fun setDefaultNightMode(nightMode: Int)

        fun setLocationBasedNightMode(locationBased: Boolean)

        fun setTimerKeepsScreenOn(keepsScreenOn: Boolean)

        fun setAllowsFullscreenTimer(allowsFullscreen: Boolean)

        //endregion

        //region Notification prefs

        fun setUsePersistentNotification(usePersistent: Boolean)

        fun setNotificationSoundEnabled(soundEnabled: Boolean)

        fun setNotificationVibrateEnabled(vibrateEnabled: Boolean)

        fun setNotificationLedEnabled(lightEnabled: Boolean)

        fun setNotificationLedColor(@ColorInt argb: Int)

        //endregion
    }
}