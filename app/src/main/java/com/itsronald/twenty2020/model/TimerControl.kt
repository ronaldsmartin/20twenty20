package com.itsronald.twenty2020.model

import android.support.annotation.IntDef

/**
 * Commands enabling control of a Cycle's timer.
 */
interface TimerControl {

    companion object {
        //region TimerEvent

        @IntDef(TIMER_STARTED, TIMER_PAUSED, TIMER_RESTARTED, TIMER_SKIPPED_PHASE)
        @Retention(AnnotationRetention.SOURCE)
        annotation class TimerEvent

        /** Event: timer was started. */
        const val TIMER_STARTED = 1L
        /** Event: timer was paused. */
        const val TIMER_PAUSED = 2L
        /** Event: timer was restarted. */
        const val TIMER_RESTARTED = 3L
        /** Event: timer was skipped to the next phase. */
        const val TIMER_SKIPPED_PHASE = 4L

        /**
         * Get the name of a numeric [TimerEvent].
         *
         * @param event The TimerEvent whose name to compute.
         * @return The name of [event].
         */
        fun eventName(@TimerEvent event: Long): String = when (event) {
            TIMER_STARTED       -> "TIMER_STARTED"
            TIMER_PAUSED        -> "TIMER_PAUSED"
            TIMER_RESTARTED     -> "TIMER_RESTARTED"
            TIMER_SKIPPED_PHASE -> "TIMER_SKIPPED_PHASE"
            else -> throw IllegalArgumentException("Event $event is not a valid @TimerEvent.")
        }

        //endregion
    }

    /** Whether or not the timer is running. */
    val running: Boolean

    /**
     * If the cycle timer is running, pause it. Otherwise, start or resume its timer.
     */
    fun toggleRunning()

    /**
     * Restart the time remaining in the current phase to its original duration.
     */
    fun restartPhase()

    /**
     * Immediately end the current phase and start the next phase.
     */
    fun startNextPhase(delay: Int = 0)
}