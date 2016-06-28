package com.itsronald.twenty2020.settings

import android.support.annotation.ColorInt
import com.itsronald.twenty2020.base.Presenter
import com.itsronald.twenty2020.base.View

interface SettingsContract {

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