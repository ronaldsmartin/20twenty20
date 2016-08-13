package com.itsronald.twenty2020

import android.app.Application
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.itsronald.twenty2020.alarms.AlarmModule
import com.itsronald.twenty2020.data.DaggerResourceComponent
import com.itsronald.twenty2020.data.ResourceModule
import com.itsronald.twenty2020.notifications.NotificationModule
import com.itsronald.twenty2020.reporting.CrashLogTree
import com.itsronald.twenty2020.settings.DaggerPreferencesComponent
import com.itsronald.twenty2020.settings.PreferencesModule
import com.karumi.dexter.Dexter
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric

import timber.log.Timber

class Twenty2020Application : Application() {

    companion object {
        /** Accessor for the singleton Application object. */
        lateinit var INSTANCE: Twenty2020Application
            private set
    }

    /** Dagger component that vends singleton dependencies. */
    val appComponent: ApplicationComponent = {
        val resourceComponent = DaggerResourceComponent.builder()
                .resourceModule(ResourceModule(this))
                .build()
        val preferencesComponent = DaggerPreferencesComponent.builder()
                .preferencesModule(PreferencesModule(this))
                .build()

        DaggerApplicationComponent.builder()
                .resourceComponent(resourceComponent)
                .preferencesComponent(preferencesComponent)
                .notificationModule(NotificationModule(this))
                .alarmModule(AlarmModule(this))
                .build()
    }()

    override fun onCreate() {
        super.onCreate()
        init()

        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)
        useDefaultNightMode()
    }

    private fun init() {
        INSTANCE = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("Timber logger planted.")
        }
        LeakCanary.install(this)
        Dexter.initialize(this)
        Fabric.with(this, Crashlytics())
        Timber.plant(CrashLogTree())

        // Just referencing these ensures that they are instantiated by Dagger.
        appComponent.alarmScheduler()
        appComponent.notifier()
    }

    private fun useDefaultNightMode() {
        val sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(this)
        val nightModeKey = getString(R.string.pref_key_display_night_mode)
        sharedPrefs.getString(nightModeKey, null)?.toInt()?.let {
            if (it == AppCompatDelegate.MODE_NIGHT_YES
                || it == AppCompatDelegate.MODE_NIGHT_NO
                || it == AppCompatDelegate.MODE_NIGHT_AUTO) {
                Timber.v("Setting DayNight Mode to last stored preference.")
                AppCompatDelegate.setDefaultNightMode(it)
            }
        }
    }
}
