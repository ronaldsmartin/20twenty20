package com.itsronald.twenty2020.base

import android.content.Context

/**
 * Base interface for Views.
 */
interface View {

    var presenter: Presenter<View>?

    val context: Context
}