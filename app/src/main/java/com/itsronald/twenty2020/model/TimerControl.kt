package com.itsronald.twenty2020.model

/**
 * Commands enabling control of a Cycle's timer.
 */
interface TimerControl {

    /** Whether or not the timer is running. */
    val running: Boolean

    /**
     * If the cycle timer is running, pause it. Otherwise, start or resume its timer.
     */
    fun toggleRunning()

    /**
     * Immediately end the current phase and start the next phase.
     */
    fun restartPhase()

    /**
     * Restart the time remaining in the current phase to its original duration.
     */
    fun startNextPhase(delay: Int = 0)
}