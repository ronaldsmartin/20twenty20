package com.itsronald.twenty2020.settings

import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.notifications.NotificationHelper
import dagger.Component

@Component(modules = arrayOf(SettingsModule::class))
interface SettingsComponent {
    fun preferences(): RxSharedPreferences
    fun inject(notificationHelper: NotificationHelper)
}