package com.itsronald.twenty2020.timer

import com.itsronald.twenty2020.base.Activity
import dagger.Module
import dagger.Provides

@Module
class TimerModule(private val view: TimerContract.TimerView) {

    @Provides @Activity
    fun provideTimerView(): TimerContract.TimerView = view

    @Provides @Activity
    fun providePresenter(presenter: TimerPresenter): TimerContract.UserActionsListener = presenter
}