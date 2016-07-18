package com.itsronald.twenty2020.data

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ResourceModule(private val context: Context) {

    @Provides
    fun provideResourceRepository(): ResourceRepository = ContextResourceRepository(context)
}