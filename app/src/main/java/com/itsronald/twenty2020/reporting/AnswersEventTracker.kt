package com.itsronald.twenty2020.reporting

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AnswersEventTracker
    @Inject constructor(private val answers: Answers) : EventTracker {

    override fun reportEvent(event: EventTracker.Event) {
        Timber.i("Reporting event '$event' to Fabric Answers kit.")
        answers.logCustom(event.toAnswersEvent())
    }

    private fun EventTracker.Event.toAnswersEvent(): CustomEvent {
        val event = CustomEvent(this.name)
                .putCustomAttribute("Category", this.category)

        // Add any custom attributes for specific events.
        for ((name, value) in attributes) {
            when (value) {
                is EventTracker.Event.AttributeValue.Number ->
                        event.putCustomAttribute(name, value.number)
                is EventTracker.Event.AttributeValue.String ->
                        event.putCustomAttribute(name, value.string)
            }
        }

        return event
    }
}