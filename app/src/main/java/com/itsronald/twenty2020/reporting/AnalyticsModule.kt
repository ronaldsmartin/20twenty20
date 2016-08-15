package com.itsronald.twenty2020.reporting

import com.crashlytics.android.answers.Answers
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AnalyticsModule {

    @Provides @Singleton
    fun provideAnswers(): Answers = Answers.getInstance()

    @Provides @Singleton
    fun provideEventTracker(answersTracker: AnswersEventTracker): EventTracker = answersTracker
}