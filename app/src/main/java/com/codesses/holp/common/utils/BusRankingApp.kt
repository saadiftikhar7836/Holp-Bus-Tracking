package com.codesses.holp.common.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import com.codesses.holp.common.views.listeners.Bus
import com.codesses.holp.common.views.model.BusStations

class BusRankingApp : Application() {

    private lateinit var mContext: Context


    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        androidId = Settings.Secure.getString(contentResolver,
            Settings.Secure.ANDROID_ID)
        mContext = applicationContext
        SharedPrefConfig.initSharedConfig(configBus)
        saveBusStations()

    }

    companion object {
        private var list = ArrayList<BusStations>()
        var androidId = ""
        fun getBusStations(): List<BusStations> = list.toMutableList()
    }

    private val configBus = object : Bus {
        override fun getAppContext(): Context {
            return this@BusRankingApp
        }
    }

    private fun saveBusStations() {
        list.add(BusStations(18.50386961, 73.92194096, "1"))
        list.add(BusStations(18.5061766, 18.5061766, "2"))
        list.add(BusStations(18.50644097, 73.90758649, "3"))
        list.add(BusStations(18.50640238, 73.90191507, "4"))
        list.add(BusStations(18.50668386, 73.89689884, "5"))
        list.add(BusStations(18.50631114, 73.89063955, "6"))
        list.add(BusStations(18.5060994, 73.88227417, "7"))
        list.add(BusStations(18.50237404, 73.87689638, "8"))
        list.add(BusStations(18.50165486, 73.87071179, "9"))
        list.add(BusStations(18.50095899, 73.867973, "10"))
        list.add(BusStations(18.49991438, 73.86379424, "11"))
        list.add(BusStations(18.50011319, 73.86123088, "12"))
    }
}