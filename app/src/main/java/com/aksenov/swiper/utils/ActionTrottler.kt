package com.aksenov.swiper.utils

import android.os.SystemClock

object ActionThrottler {

    private var lastActionTime = 0L

    fun throttleAction(delay: Long, action: () -> Unit): Boolean {
        val currentTime = SystemClock.elapsedRealtime()
        val diff = currentTime - lastActionTime

        return if (diff >= delay) {
            lastActionTime = currentTime
            action.invoke()
            true
        } else {
            false
        }
    }

}