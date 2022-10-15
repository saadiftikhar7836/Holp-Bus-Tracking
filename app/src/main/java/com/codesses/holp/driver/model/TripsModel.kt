package com.codesses.holp.driver.model

data class TripsModel(
    var bus_no: String = "",
    var bus_plate_no: String = "",
    var status: Int = 0,
    var driver_lat: Double = 0.0,
    var driver_lng: Double = 0.0,
    var timestamp: Long = 0L,
    var driver_id: String = ""
) {
    var tripId = ""
}
