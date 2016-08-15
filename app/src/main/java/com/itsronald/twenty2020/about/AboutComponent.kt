package com.itsronald.twenty2020.about

import com.itsronald.twenty2020.data.ResourceComponent
import dagger.Component

@Component(dependencies = arrayOf(ResourceComponent::class))
interface AboutComponent {
    fun aboutPresenter(): AboutPresenter
}