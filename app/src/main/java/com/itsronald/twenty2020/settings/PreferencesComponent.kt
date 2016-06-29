package com.itsronald.twenty2020.settings

import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.notifications.NotificationHelper
import dagger.Component

@Component(modules = arrayOf(PreferencesModule::class))
interface PreferencesComponent {
    fun preferences(): RxSharedPreferences
    fun inject(notificationHelper: NotificationHelper)
}