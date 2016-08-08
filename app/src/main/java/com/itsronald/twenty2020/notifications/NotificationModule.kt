package com.itsronald.twenty2020.notifications

import android.content.Context
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.data.ResourceRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NotificationModule(private val context: Context) {

    @Provides @Singleton
    fun provideNotifier(preferences: RxSharedPreferences, resources: ResourceRepository): Notifier =
            Notifier(context = context, preferences = preferences, resources = resources)
}