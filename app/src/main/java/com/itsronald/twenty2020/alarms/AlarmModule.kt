package com.itsronald.twenty2020.alarms

import android.app.AlarmManager
import android.content.Context
import com.itsronald.twenty2020.model.Cycle
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AlarmModule() {

    @Provides
    fun provideAlarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}