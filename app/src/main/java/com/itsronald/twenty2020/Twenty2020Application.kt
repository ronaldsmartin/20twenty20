package com.itsronald.twenty2020

import android.app.Application
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.data.DaggerResourceComponent
import com.itsronald.twenty2020.data.ResourceModule
import com.itsronald.twenty2020.model.CycleComponent
import com.itsronald.twenty2020.model.CycleModule
import com.itsronald.twenty2020.model.DaggerCycleComponent
import com.itsronald.twenty2020.settings.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.PreferencesModule
import com.karumi.dexter.Dexter
import com.squareup.leakcanary.LeakCanary

import timber.log.Timber

class Twenty2020Application : Application() {

    val cycleComponent: CycleComponent = DaggerCycleComponent.builder()
            .resourceComponent(DaggerResourceComponent.builder()
                    .resourceModule(ResourceModule(this))
                    .build())
            .cycleModule(CycleModule()).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("Timber logger planted.")
        }
        LeakCanary.install(this)
        Dexter.initialize(this)
        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)
        useDefaultNightMode()
    }

    private fun useDefaultNightMode() {
        val sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(this)
        val preferences  = RxSharedPreferences.create(sharedPrefs)
        val nightModeKey = getString(R.string.pref_key_display_night_mode)
        preferences.getString(nightModeKey)
                .get()?.toInt()?.let {
            if (it == AppCompatDelegate.MODE_NIGHT_YES
                || it == AppCompatDelegate.MODE_NIGHT_NO
                || it == AppCompatDelegate.MODE_NIGHT_AUTO) {
                Timber.v("Setting DayNight Mode to last stored preference.")
                AppCompatDelegate.setDefaultNightMode(it)
            }
        }
    }
}
