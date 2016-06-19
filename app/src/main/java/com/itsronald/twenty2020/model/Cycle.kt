package com.itsronald.twenty2020.model

/**
 * A value representing the repeating work and break cycle in the app.
 */
class Cycle {

    /**
     * The alternating phases of the 20-20-20 cycle.
     */
    enum class Phase {
        /**
         * The longer phase of the cycle.
         */
        WORK,
        /**
         * The shorter phase of the cycle.
         */
        BREAK;

        /**
         * The default duration for this phase.
         */
        val defaultDuration: Int
            get() = when(this) {
                WORK  -> 60 // 60 seconds
                BREAK -> 30 // 30 seconds
            }

        /**
         * The next sequential phase that follows this phase.
         */
        val nextPhase: Phase
            get() = when(this) {
                WORK  -> BREAK
                BREAK -> WORK
            }
    }
}