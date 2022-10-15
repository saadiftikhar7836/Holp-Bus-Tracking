package com.codesses.holp.common.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.codesses.holp.driver.model.TripsModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.concurrent.TimeUnit


fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

fun isLocationPermission(mContext: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        mContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        mContext,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun getLatLngAddress(context: Context, latitude: Double, longitude: Double): String {
    val geocoder = Geocoder(context)

    return try {
        val addressList = geocoder.getFromLocation(
            latitude,
            longitude,
            1

        )
        (addressList as MutableList<Address>?)?.get(0)
            ?.getAddressLine(0)
            ?: "" // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
    } catch (e: Exception) {
        Log.d("GET_ADDRESS", "getAddress: ${e.message}")
        ""
    }
}

fun Long.timeToTimeAgo(past: Long): String {
    if (past == 0L) {
        return ""
    }

    val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(this - past)
    val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(this - past)
    val hours: Long = TimeUnit.MILLISECONDS.toHours(this - past)
    val days: Long = TimeUnit.MILLISECONDS.toDays(this - past)

    return when {
        seconds < 60 -> {
            "Rates updated $seconds second${if (seconds > 1) "s" else ""} ago"
        }
        minutes < 60 -> {
            "Rates updated $minutes minute${if (minutes > 1) "s" else ""} ago"
        }
        hours < 24 -> {
            "Rates updated $hours hour${if (hours > 1) "s" else ""} ago"
        }
        else -> {
            "Rates updated $days day${if (days > 1) "s" else ""} ago"
        }
    }
}

fun DocumentSnapshot.toTripsModel(): TripsModel? {
    return try {
        val tripsModel = this.toObject(TripsModel::class.java)
        tripsModel?.tripId = this.id
        tripsModel
    } catch (e: Exception) {
        this.toTripsModel()
    }
}


