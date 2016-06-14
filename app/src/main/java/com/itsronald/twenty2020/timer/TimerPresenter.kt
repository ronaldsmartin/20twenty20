package com.itsronald.twenty2020.timer

import android.content.Intent
import android.os.Bundle
import com.itsronald.twenty2020.settings.SettingsActivity


class TimerPresenter(timerView: TimerContract.TimerView): TimerContract.UserActionsListener {

    override var view: TimerContract.TimerView? = timerView

    override fun onCreate(bundle: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun onStart() {
        throw UnsupportedOperationException()
    }

    override fun onStop() {
        throw UnsupportedOperationException()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        throw UnsupportedOperationException()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        throw UnsupportedOperationException()
    }

    override fun toggleCycleRunning() {
        throw UnsupportedOperationException()
    }

    override fun delayCycle() {
        throw UnsupportedOperationException()
    }

    override fun restartCycle() {
        throw UnsupportedOperationException()
    }

    override fun openSettings() {
        val context = view?.context
        if (context != null) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun openHelpFeedback() {
        throw UnsupportedOperationException()
    }

}