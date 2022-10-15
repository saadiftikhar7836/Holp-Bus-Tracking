package com.codesses.holp.common.utils

import com.codesses.holp.driver.model.TripsModel


object SharedStorage : SharedPrefHelper(SharedConfig.PREF_NAME) {

    private const val IS_TRIP_STARTED = "is_trip_started"
    private const val CURRENT_TRIP = "current_trip"
    private const val ACTIVE_BUS_ID = "active_bus_id"


    @Synchronized
    fun saveActiveBusId(value: String) {
        saveString(ACTIVE_BUS_ID, value)
    }

    @Synchronized
    fun getActiveBusId(): String {
        return getString(ACTIVE_BUS_ID, "")
    }

    @Synchronized
    fun removeBusId() {
        removeKey(ACTIVE_BUS_ID)
    }


    @Synchronized
    fun saveCurrentTrip(obj: TripsModel) {
        saveObject(CURRENT_TRIP, obj)
    }

    fun getCurrentTrip(): TripsModel {
        return getObject(CURRENT_TRIP, TripsModel::class.java) as TripsModel
    }

    fun removeCurrentTrip() {
        removeKey(CURRENT_TRIP)
    }


    @Synchronized
    fun saveTripStart(value: Boolean) {
        saveBoolean(IS_TRIP_STARTED, value)
    }

    fun isTripStarted(): Boolean {
        return getBoolean(IS_TRIP_STARTED, false)
    }


}