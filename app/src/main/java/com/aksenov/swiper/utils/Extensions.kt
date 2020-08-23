package com.aksenov.swiper.utils

import android.content.res.Resources

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.px: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
