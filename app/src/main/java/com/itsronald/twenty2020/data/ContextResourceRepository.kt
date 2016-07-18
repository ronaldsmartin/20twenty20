package com.itsronald.twenty2020.data

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat

/**
 * Concrete implementation of [ResourceRepository] that uses a [Context] to provide requested
 * resouces.
 */
class ContextResourceRepository(private val context: Context) : ResourceRepository {

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
            context.getString(resId, *formatArgs)

    override fun getColor(@ColorRes resId: Int): Int = ContextCompat.getColor(context, resId)
}