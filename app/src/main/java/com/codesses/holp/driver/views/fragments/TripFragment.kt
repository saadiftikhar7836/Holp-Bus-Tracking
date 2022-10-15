package com.codesses.holp.driver.views.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codesses.holp.R
import com.codesses.holp.common.firestore.FirestoreRef
import com.codesses.holp.common.utils.BusRankingApp
import com.codesses.holp.common.utils.ProgressDialog
import com.codesses.holp.common.utils.SharedStorage
import com.codesses.holp.common.utils.toTripsModel
import com.codesses.holp.databinding.FragmentTripBinding
import com.codesses.holp.driver.model.TripsModel
import com.codesses.holp.service.MyLocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration

class TripFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentTripBinding
    lateinit var tripModel: TripsModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var map: GoogleMap
    private var busMarker: Marker? = null
    private lateinit var snapshot: ListenerRegistration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripModel = SharedStorage.getCurrentTrip()
        progressDialog = ProgressDialog(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTripBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        setupMapView()
    }

    override fun onResume() {
        binding.mapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onClick(view: View) {
        when (view) {
            binding.btnStopTrip -> {

                SharedStorage.saveTripStart(false)
                SharedStorage.removeCurrentTrip()

                requireActivity().stopService(
                    Intent(requireActivity(), MyLocationService::class.java)
                )
                MyLocationService.mLocationManager?.removeUpdates(MyLocationService.locationListener)
                progressDialog.show()
                stopTrip()
            }
        }
    }

    private fun setupMapView() {
        binding.mapView.getMapAsync {
            map = it
            addDynamicBusMarker()
            addMarkers()
        }

        binding.btnStopTrip.setOnClickListener(this)

    }

    private fun addDynamicBusMarker() {
        snapshot = FirestoreRef
            .getTripsRef()
            .document(tripModel.tripId)
            .addSnapshotListener { value, _ ->
                value?.let { documentSnapshot ->
                    val newTripModel = documentSnapshot.toTripsModel()
                    if (busMarker == null) {
                        createMarker(newTripModel)
                    } else {
                        updateMarker(newTripModel)
                    }
                }
            }
    }

    private fun stopTrip() {
        FirestoreRef.getTripsRef()
            .document(tripModel.tripId)
            .update("status", 0)
            .addOnCompleteListener {
                changeActiveBusStatus()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
            }
    }

    private fun changeActiveBusStatus() {
        FirestoreRef.getActiveBus(SharedStorage.getActiveBusId())
            .update("is_bus_busy", 0)
            .addOnSuccessListener {
                snapshot.remove()
                SharedStorage.removeBusId()
                progressDialog.dismiss()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
            }
    }

    private fun bitmapDescriptorFromVector(@DrawableRes vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context ?: requireActivity(), vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun cameraUpdate(latLng: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }

    private fun createMarker(newTripModel: TripsModel?) {
        val latLng = LatLng(
            newTripModel?.driver_lat
                ?: tripModel.driver_lat,
            newTripModel?.driver_lng
                ?: tripModel.driver_lng
        )
        val markerOptions = MarkerOptions()
            .position(
                latLng
            )
            .title("me")
            .icon(bitmapDescriptorFromVector(R.drawable.ic_bus))
        busMarker = map.addMarker(markerOptions)
        cameraUpdate(latLng)
    }

    private fun updateMarker(newTripModel: TripsModel?) {
        busMarker?.let {
            it.position = LatLng(
                newTripModel?.driver_lat
                    ?: tripModel.driver_lat,
                newTripModel?.driver_lng
                    ?: tripModel.driver_lng
            )
        }
    }

    private fun addMarkers() {
        BusRankingApp.getBusStations().forEach {
            val markerOptions = MarkerOptions()
            markerOptions
                .position(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
                .title(it.stopNo)
                .icon(
                    bitmapDescriptorFromVector(R.drawable.bus_stop)
                )

            map.addMarker(markerOptions)
        }

    }
}