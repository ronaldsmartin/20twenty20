package com.itsronald.twenty2020.timer

import com.f2prateek.rx.preferences.RxSharedPreferences
import com.itsronald.twenty2020.base.Activity
import com.itsronald.twenty2020.model.Cycle
import dagger.Module
import dagger.Provides

@Module
class TimerModule(private val view: TimerContract.TimerView) {

    @Provides
    @Activity
    fun provideTimerView(): TimerContract.TimerView = view

    @Provides
    @Activity
    fun providePresenter(cycle: Cycle,
                         preferences: RxSharedPreferences): TimerContract.UserActionsListener =
            TimerPresenter(view, cycle, preferences)
}