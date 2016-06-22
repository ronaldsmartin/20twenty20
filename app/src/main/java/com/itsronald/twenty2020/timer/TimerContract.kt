package com.itsronald.twenty2020.timer

import android.support.annotation.DrawableRes
import com.itsronald.twenty2020.base.Presenter
import com.itsronald.twenty2020.base.View
import com.itsronald.twenty2020.model.TimerControl

/**
 * Specifies the contract between the timer view and its presenter.
 */
interface TimerContract {

    companion object {
        /** Activity action: Pause the timer. */
        val ACTION_PAUSE = "com.itsronald.twenty2020.timer.ACTION_PAUSE"
    }

    interface TimerView: View<UserActionsListener> {

        fun showTimeRemaining(formattedTime: String)

        fun showMajorProgress(progress: Int, maxProgress: Int)

        fun showMinorProgress(progress: Int, maxProgress: Int)

        fun setFABDrawable(@DrawableRes drawableId: Int)
    }

    interface UserActionsListener: Presenter<TimerView>, TimerControl {

        /// Menu options

        /**
         * Open the app settings.
         */
        fun openSettings()

        /**
         * Open the Help/Feedback page.
         */
        fun openHelpFeedback()
    }

}