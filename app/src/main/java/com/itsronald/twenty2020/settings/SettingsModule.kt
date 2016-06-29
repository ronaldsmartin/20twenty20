package com.itsronald.twenty2020.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences.RxSharedPreferences
import dagger.Module
import dagger.Provides

@Module
class SettingsModule(private val context: Context) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    fun provideRxPreferences(sharedPreferences: SharedPreferences): RxSharedPreferences =
            RxSharedPreferences.create(sharedPreferences)
}