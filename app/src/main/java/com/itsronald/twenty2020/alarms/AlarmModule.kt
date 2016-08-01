package com.itsronald.twenty2020.alarms

import android.app.AlarmManager
import android.content.Context
import com.itsronald.twenty2020.model.Cycle
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AlarmModule(private val context: Context) {

    @Provides
    fun provideAlarmManager(): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides @Singleton
    fun provideAlarmScheduler(alarmManager: AlarmManager, cycle: Cycle): AlarmScheduler =
            AlarmScheduler(context = context, alarmManager = alarmManager, cycle = cycle)
}