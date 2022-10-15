package com.codesses.holp.common.utils

import android.content.Context
import com.codesses.holp.common.views.listeners.Bus


object SharedPrefConfig {
    private lateinit var mBus: Bus

    fun initSharedConfig(bus: Bus) {
        synchronized(this) {
            mBus = bus
        }
    }

    fun getAppContext(): Context {
        return mBus.getAppContext()
    }
}