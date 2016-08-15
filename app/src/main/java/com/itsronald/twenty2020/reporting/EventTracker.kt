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

        //region Events

        class TimerStarted(val cycle: Cycle) : Event() {
            override val category: String = "Timer"
            override val name: String = "Timer Started"
            override val attributes: Map<String, AttributeValue> = mapOf(
                    "Phase" to AttributeValue.String(cycle.phaseName),
                    "Duration" to AttributeValue.Number(cycle.duration),
                    "Elapsed Time" to AttributeValue.Number(cycle.elapsedTime)
            )
        }
        class TimerPaused(val cycle: Cycle): Event() {
            override val category: String = "Timer"
            override val name: String = "Timer Paused"
            override val attributes: Map<String, AttributeValue> = mapOf(
                    "Phase" to AttributeValue.String(cycle.phaseName),
                    "Duration" to AttributeValue.Number(cycle.duration),
                    "Elapsed Time" to AttributeValue.Number(cycle.elapsedTime)
            )
        }

        //endregion
    }
}