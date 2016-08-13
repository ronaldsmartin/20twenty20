package com.itsronald.twenty2020.reporting

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * A Timber Tree that logs to crash reporting services.
 */
class CrashLogTree : Timber.Tree() {

    override fun isLoggable(priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        // Add any additional log recipients here.
        if (t == null) {
            Crashlytics.log(priority, tag, message)
        } else {
            Crashlytics.logException(t)
        }
    }
}