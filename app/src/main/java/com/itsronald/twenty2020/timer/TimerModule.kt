package com.itsronald.twenty2020.timer

import dagger.Module
import dagger.Provides

@Module
class TimerModule(private val view: TimerContract.TimerView) {

    @Provides
    fun provideTimerView(): TimerContract.TimerView = view

    @Provides
    fun providePresenter(): TimerContract.UserActionsListener = TimerPresenter(view)
}