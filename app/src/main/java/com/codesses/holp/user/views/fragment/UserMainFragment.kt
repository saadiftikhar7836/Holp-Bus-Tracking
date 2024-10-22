package com.codesses.holp.user.views.fragment

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codesses.holp.R
import com.codesses.holp.common.utils.getLatLngAddress
import com.codesses.holp.common.utils.isLocationPermission
import com.codesses.holp.common.utils.showToast
import com.codesses.holp.databinding.FragmentUserMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlin.properties.Delegates


class UserMainFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentUserMainBinding


    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    private lateinit var locationCallback: LocationCallback
    private var location: Location? = null

    private var isSearchStartLoc: Boolean = false
    private var isSearchEndLoc: Boolean = false
    private var hasNetwork: Boolean = false
    private var startLat: Double = 0.0
    private var startLng: Double = 0.0
    private var endLat: Double = 0.0
    private var endLng: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        setupInitialization()
        setupView()

    }


    override fun onClick(view: View?) {
        when (view) {
            binding.includeHeader.ivBackPress -> findNavController().popBackStack()
            binding.ivCurrentLoc -> getCurrentLocation()
            binding.ivSearchStartLoc -> {
                isSearchStartLoc = true
                isSearchEndLoc = false
                searchLocation()
            }
            binding.ivSearchEndLoc -> {
                isSearchEndLoc = true
                isSearchStartLoc = false
                searchLocation()
            }
            binding.btnAction -> {
                if (isLatLngInitialized())
                    findNavController().navigate(
                        UserMainFragmentDirections.actionUserMainFragmentToBusListBottomSheet()
                            .setStartLat(startLat.toFloat())
                            .setStartLng(startLng.toFloat())
                            .setEndLat(endLat.toFloat())
                            .setEndLng(endLng.toFloat())
                    )
                else
                    requireActivity().showToast("Start and destination location is required")
            }
        }
    }

    // Auto complete search listener
    private var autoCompleteSearchLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val place = result.data?.let { Autocomplete.getPlaceFromIntent(it) }

                    place?.let {
                        if (isSearchStartLoc) {
                            binding.tvSearchedStartLoc.text = it.address
                            startLat = it.latLng?.latitude ?: 0.0
                            startLng = it.latLng?.longitude ?: 0.0
                        } else {
                            binding.tvSearchedEndLoc.text = it.address

                            endLat = it.latLng?.latitude ?: 0.0
                            endLng = it.latLng?.longitude ?: 0.0
                        }
                    }
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    val status = result.data?.let { Autocomplete.getStatusFromIntent(it) }
                    status?.let { Log.d("SEARCH_STATUS", it.statusMessage.toString()) }
                }

                Activity.RESULT_CANCELED -> {
                    Log.d("SEARCH_CANCEL", "User cancelled the operation")
                }
            }

        }

    private fun setupInitialization() {

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (!Places.isInitialized()) {
            Places.initialize(
                requireActivity().applicationContext,
                requireActivity().getString(R.string.google_map_api_key)
            )
        }
    }

    private fun setupView() {
        binding.includeHeader.tvTitle.text = requireActivity().getString(R.string.choose_location)

        binding.mapView.getMapAsync {
            map = it
            getCurrentLocation()
        }

        binding.includeHeader.ivBackPress.setOnClickListener(this)
        binding.ivCurrentLoc.setOnClickListener(this)
        binding.ivSearchStartLoc.setOnClickListener(this)
        binding.ivSearchEndLoc.setOnClickListener(this)
        binding.btnAction.setOnClickListener(this)

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

    private fun isLatLngInitialized(): Boolean {
        return startLat != 0.0 && startLng != 0.0 && endLat != 0.0 && endLng != 0.0
    }

    private fun searchLocation() {
        val placesList = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placesList)
            .build(requireActivity())
        autoCompleteSearchLauncher.launch(intent)
    }

    private fun getCurrentLocation() {
        if (isLocationPermission(requireActivity()))
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location = it
                location?.let { loc ->
                    cameraUpdate(loc.latitude, loc.longitude)
                } ?: kotlin.run {
                    requestFusedLocation()
                }
            }
    }

    private fun requestFusedLocation() {

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                location = locationResult.lastLocation
                location?.let {
                    cameraUpdate(it.latitude, it.longitude)
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

    private fun cameraUpdate(lat: Double, lng: Double) {
        startLat = lat
        startLng = lng
        val address = getLatLngAddress(requireActivity(), lat, lng)
        val latLng = LatLng(lat, lng)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(address)
        map.clear()
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        map.addMarker(markerOptions)

        binding.tvSearchedStartLoc.text = address
    }

}