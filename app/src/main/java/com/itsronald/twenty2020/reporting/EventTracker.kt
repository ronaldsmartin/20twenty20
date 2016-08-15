package com.itsronald.twenty2020.reporting

import com.itsronald.twenty2020.model.Cycle

/**
 * A tracker for analytics events.
 *
 * Implements SDK-specific reporting mechanisms and defines events that can be reported.
 *
 * @see Event
 */
interface EventTracker {

    /**
     * Report a new event to this event tracker.
     *
     * @param event The event to be tracked.
     */
    fun reportEvent(event: Event)

    /**
     * An event that can be tracked/reported to an [EventTracker].
     */
    sealed class Event {

        companion object {
            const val CATEGORY_TIMER = "Timer"
        }

        abstract val category: String
        abstract val name: String
        abstract val attributes: Map<String, AttributeValue>

        /**
         * A type-safe alias for possible attribute values.
         *
         * Most analytics APIs limit parameters to JSON-encodable values. Numbers and Strings are
         * enough to suffice for our purposes, however.
         */
        sealed class AttributeValue {
            class Number(val number: kotlin.Number) : AttributeValue()
            class String(val string: kotlin.String) : AttributeValue()
        }

        //region Timer Events

        class TimerStarted(val cycle: Cycle) : Event() {
            override val category: String = CATEGORY_TIMER
            override val name: String = "Timer Started"
            override val attributes: Map<String, AttributeValue> = timerEventAttributes(cycle)
        }
        class TimerPaused(val cycle: Cycle) : Event() {
            override val category: String = CATEGORY_TIMER
            override val name: String = "Timer Paused"
            override val attributes: Map<String, AttributeValue> = timerEventAttributes(cycle)
        }
        class TimerRestarted(val cycle: Cycle) : Event() {
            override val category: String = CATEGORY_TIMER
            override val name: String = "Timer Restarted"
            override val attributes: Map<String, AttributeValue> = timerEventAttributes(cycle)
        }
        class TimerPhaseSkipped(val cycle: Cycle) : Event() {
            override val category: String = CATEGORY_TIMER
            override val name: String = "Timer Phase Skipped"
            override val attributes: Map<String, AttributeValue> = timerEventAttributes(cycle)
        }

        /**
         * Common data of interest when tracking timer events.
         */
        protected fun timerEventAttributes(cycle: Cycle): Map<String, AttributeValue> = mapOf(
                "Phase" to AttributeValue.String(cycle.phaseName),
                "Duration" to AttributeValue.Number(cycle.duration),
                "Elapsed Time" to AttributeValue.Number(cycle.elapsedTime)
        )

        //endregion
    }
}