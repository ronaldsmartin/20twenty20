package com.itsronald.twenty2020.base

import android.content.Context

/**
 * Base interface for Views.
 */
interface View<Presenter> {

    var presenter: Presenter

    val context: Context
}