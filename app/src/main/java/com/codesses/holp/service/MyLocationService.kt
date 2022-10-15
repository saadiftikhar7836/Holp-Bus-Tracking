package com.codesses.holp.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.codesses.holp.R
import com.codesses.holp.common.firestore.FirestoreRef
import com.codesses.holp.common.utils.SharedStorage
import com.codesses.holp.driver.model.TripsModel


class MyLocationService : Service() {


    companion object {
        var mContext: Context? = null
        var mLocationManager: LocationManager? = null
        val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val tripModel = SharedStorage.getObject("trip", TripsModel::class.java) as TripsModel
                tripModel.driver_lng = location.longitude
                tripModel.driver_lat = location.latitude
                val hashMap = HashMap<String, Any>()
                hashMap["driver_lng"] = location.longitude
                hashMap["driver_lat"] = location.latitude
                FirestoreRef
                    .getTripsRef()
                    .document(tripModel.tripId)
                    .update(hashMap)
                    .addOnCompleteListener {

                    }
                // Create notification
                if (SDK_INT >= O)
                    createNotification();
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String) {
                Log.e("Manager", provider);
                if (ActivityCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                } else {
                    if (mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true)
                        mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, LOCATION_DISTANCE, this);
                    else if (mLocationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true)
                        mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, LOCATION_DISTANCE, this);

                }
            }

            @Override
            override fun onProviderDisabled(provider: String) {
                Log.e("Manager", provider);
            }

        }

        @RequiresApi(api = O)
        fun createNotification(): Notification {
            val channel = NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager?.createNotificationChannel(channel);

            val notification = Notification.Builder(mContext, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle("Location Service Running")
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColorized(true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(false)
                .build();
            // Notification ID cannot be 0.
            return notification

        }

        const val LOCATION_DISTANCE = 100f
        private const val CHANNEL_DEFAULT_IMPORTANCE = "my service notification"
        var notificationManager: NotificationManager? = null
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate();
        mContext = applicationContext;
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (SDK_INT >= O) {
            startForeground(1, createNotification())
        }
        initializeLocationManager();
        postLocation();

    }


    private fun postLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;

        } else {
            if (mLocationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true)
                mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, LOCATION_DISTANCE, locationListener);
            else if (mLocationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true)
                mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, LOCATION_DISTANCE, locationListener);

        }
    }


    private fun initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
    }


}