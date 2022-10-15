package com.codesses.holp.driver.views.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codesses.holp.common.firestore.FirestoreRef
import com.codesses.holp.common.utils.BusRankingApp.Companion.androidId
import com.codesses.holp.common.utils.ProgressDialog
import com.codesses.holp.common.utils.SharedStorage
import com.codesses.holp.common.utils.isLocationPermission
import com.codesses.holp.common.utils.showToast
import com.codesses.holp.databinding.FragmentDriverMainBinding
import com.codesses.holp.driver.model.TripsModel
import com.codesses.holp.service.MyLocationService
import com.google.android.gms.location.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

class DriverMainFragment : Fragment(), View.OnClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    lateinit var driversRef: CollectionReference
    private lateinit var binding: FragmentDriverMainBinding
    private var currentLocation: Location? = null
    private var isDriverCreated: Boolean = false
    private var isAlreadyCreated: Boolean = false
    private var isBusNo: Boolean = false
    private var isPlateNo: Boolean = false
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        driversRef = FirestoreRef.getDriversRef(androidId)
        progressDialog = ProgressDialog(requireActivity())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    @SuppressLint("HardwareIds")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriverMainBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLocationPermission()
        binding.includeHeader.ivBackPress.setOnClickListener(this)
        binding.btnStartTrip.setOnClickListener(this)
        addTextChangedListeners()
    }

    private fun addTextChangedListeners() {
        binding.etBusNo.addTextChangedListener { editable ->
            isBusNo = editable.toString().isNotEmpty()
            updateButtonState()
        }

        binding.etBusNo.addTextChangedListener { editable ->
            isPlateNo = editable.toString().isNotEmpty()
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        binding.btnStartTrip.isEnabled = isBusNo && isPlateNo
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermission() {
        if (isLocationPermission(context ?: requireActivity())) {
            progressDialog.show()
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                currentLocation?.let { loc ->
                    driversRef.get().addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.size() == 0) {
                            createDriver()
                        } else {
                            progressDialog.dismiss()
                            isAlreadyCreated = true
                        }
                    }.addOnFailureListener {
                        requireActivity().showToast("internet error")
                    }
                } ?: kotlin.run {
                    requestFusedLocation()
                }
            }
        } else {
            requireActivity().showToast("permission denied")
        }
    }

    private fun createDriver() {
        val hashMap = HashMap<String, String>()
        hashMap["id"] = androidId
        FirestoreRef.getDriversRef(androidId)
            .add(hashMap)
            .addOnCompleteListener {
                progressDialog.dismiss()
                requireActivity().showToast("")
                isDriverCreated = true
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                requireActivity().showToast(it.localizedMessage?.toString() ?: "")
                isDriverCreated = false
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestFusedLocation() {

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation
                currentLocation?.let {
                    driversRef
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.size() == 0) {
                                createDriver()
                            } else {
                                progressDialog.dismiss()
                                isAlreadyCreated = true
                            }
                        }
                        .addOnFailureListener {
                            requireActivity().showToast("internet error")
                        }
                }
                fusedLocationClient.removeLocationUpdates(locationCallback)

            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnStartTrip -> {
                progressDialog.show()
                if (isAlreadyCreated || isDriverCreated) {
                    checkBusStatus()
                } else {
                    checkLocationPermission()
                }
            }

            binding.includeHeader.ivBackPress -> {
                findNavController().navigateUp()
            }
        }
    }

    private fun checkBusStatus() {
        FirestoreRef.getActiveBus(getBusId()).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    progressDialog.dismiss()
                    requireActivity().showToast("This bus already in use")
                } else {
                    createTrip()
                }
            }
    }

    private fun createTrip() {
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()

        hashMap["driver_id"] = androidId
        hashMap["bus_plate_no"] = binding.etBusNoPlate.text.toString()
        hashMap["bus_no"] = binding.etBusNo.text.toString()
        hashMap["driver_lat"] = currentLocation?.latitude!!
        hashMap["driver_lng"] = currentLocation?.longitude!!
        hashMap["status"] = 1
        hashMap["timestamp"] = timestamp

        FirestoreRef.getTripsRef().add(hashMap)
            .addOnSuccessListener {
                changeActiveBusStatus(it, timestamp)
            }
            .addOnFailureListener {
                requireActivity().showToast(it.localizedMessage?.toString() ?: "")
            }


    }

    private fun changeActiveBusStatus(
        documentReference: DocumentReference?,
        timestamp: Long
    ) {
        val map = HashMap<String, Any>()
        map["is_bus_busy"] = 1

        FirestoreRef.getActiveBus(getBusId()).set(map)
            .addOnSuccessListener {

                val tripModel = TripsModel()

                tripModel.driver_id = androidId
                tripModel.bus_plate_no = binding.etBusNoPlate.toString()
                tripModel.bus_no = binding.etBusNo.toString()
                tripModel.driver_lat = currentLocation?.latitude ?: 0.0
                tripModel.driver_lng = currentLocation?.longitude ?: 0.0
                tripModel.tripId = documentReference?.id ?: ""
                tripModel.timestamp = timestamp

                SharedStorage.saveCurrentTrip(tripModel)
                SharedStorage.saveTripStart(true)
                SharedStorage.saveActiveBusId(getBusId())

                if (SDK_INT >= O) {
                    requireActivity().startForegroundService(
                        Intent(requireActivity(), MyLocationService::class.java)
                    )
                } else {
                    requireActivity().startService(
                        Intent(requireActivity(), MyLocationService::class.java)
                    )
                }

                progressDialog.dismiss()

                val driverMainFragmentDirections =
                    DriverMainFragmentDirections.actionDriverMainFragmentToTripFragment()

                findNavController().navigate(driverMainFragmentDirections)
            }
    }

    private fun getBusId(): String {
        return (
                "bus_no_${
                    binding.etBusNo.text.toString().trim()
                }_plate_no_${binding.etBusNoPlate.text.toString().trim()}")
            .lowercase()
    }

}