package com.codesses.holp.common.views.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.codesses.holp.common.utils.isLocationPermission
import com.codesses.holp.common.utils.showToast
import com.codesses.holp.common.utils.startNewActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.*

class SplashScreen : AppCompatActivity() {

    private lateinit var timer: Timer

    private var task: Task<LocationSettingsResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()

    }


    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it)
                preparedLocationRequest()
            else
                showToast("permission denied")
        }

    private var resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult: ActivityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                preparedLocationRequest()
            } else {
                task?.addOnFailureListener { e ->
                    if (e is ResolvableApiException) {
                        recursiveResolutionResult(e)
                    }
                }
            }
        }


    private fun setupView() {

        timer = Timer()

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        supportActionBar?.hide()

        if (isLocationPermission(this))
            startTimer()
        else requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (!isLocationPermission(this))
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun preparedLocationRequest() {
        val locationRequest = LocationRequest.create()

        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)

        task = settingsClient.checkLocationSettings(builder.build())

        (task as Task<LocationSettingsResponse>).addOnSuccessListener(this) {
            startNewActivity(StartupActivity::class.java)
        }

        task?.addOnFailureListener(this) { e ->
            if (e is ResolvableApiException) {
                val intentSenderRequest =
                    IntentSenderRequest.Builder(e.resolution.intentSender).build()
                resolutionForResult.launch(intentSenderRequest)
            }
        }
    }

    private fun recursiveResolutionResult(e: ResolvableApiException) {
        val intentSenderRequest = IntentSenderRequest.Builder(e.resolution.intentSender).build()
        resolutionForResult.launch(intentSenderRequest)
    }

    private fun startTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {

                timer.cancel()  // It cancels the timer
                timer.purge()   // It removes all cancel timer

                startNewActivity(StartupActivity::class.java)
            }
        }, 2000)
    }

}