package com.codesses.holp.user.views.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.codesses.holp.R
import com.codesses.holp.common.firestore.FirestoreRef
import com.codesses.holp.common.utils.BusRankingApp
import com.codesses.holp.common.utils.ProgressDialog
import com.codesses.holp.common.utils.toTripsModel
import com.codesses.holp.databinding.FragmentBusDetailsBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import org.joda.time.DateTime
import java.io.IOException
import java.util.concurrent.TimeUnit


class BusDetailsFragment : Fragment() {

    private lateinit var directionsResult: DirectionsResult
    private lateinit var binding: FragmentBusDetailsBinding

    private lateinit var map: GoogleMap

    private val args: BusDetailsFragmentArgs by navArgs()

    private val markerOptionsList = ArrayList<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBusDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

        setupView()
    }

    private fun setupView() {
        binding.mapView.getMapAsync {
            map = it
            val progressDialog = ProgressDialog(requireActivity())
            addDynamicMarkers(progressDialog)
            addMarkers()
        }
    }

    private fun addDynamicMarkers(progressDialog: ProgressDialog) {
        FirestoreRef
            .getTripsRef()
            .whereNotEqualTo("status", 0)
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null && querySnapshot.size() > 0) {
                    val list = querySnapshot.map {
                        it.toTripsModel()
                    }
                    list.forEach { tripsModel ->
                        val markers = markerOptionsList.find { markerOptions ->
                            markerOptions.title == tripsModel?.driver_id
                        }
                        if (markers != null) {
                            markers
                                .position = LatLng(
                                tripsModel?.driver_lat ?: 0.0,
                                tripsModel?.driver_lng ?: 0.0
                            )
                        } else {
                            val newMarkerOptions = MarkerOptions()
                            newMarkerOptions
                                .position(
                                    LatLng(
                                        tripsModel?.driver_lat
                                            ?: 0.0, tripsModel?.driver_lng ?: 0.0
                                    )
                                )
                                .title(tripsModel?.driver_id)
                                .icon(bitmapDescriptorFromVector(R.drawable.ic_bus))
                            markerOptionsList.add(map.addMarker(newMarkerOptions)!!)
                        }

                    }
                } else {
                    if (markerOptionsList.isNotEmpty()) {
                        markerOptionsList.forEach {
                            it.remove()
                        }
                    }
                }

                progressDialog.dismiss()
            }
    }

    private fun addMarkers() {
        val markerUserStart = MarkerOptions()
        val markerUserDestination = MarkerOptions()
        markerUserStart
            .position(LatLng(args.startLat.toDouble(), args.startLng.toDouble()))
            .title("Start Location")
            .icon(bitmapDescriptorFromVector(R.drawable.ic_person_pin))
        markerUserDestination
            .position(LatLng(args.endLat.toDouble(), args.endLng.toDouble()))
            .title("Destination")
        cameraUpdate(args.startLat.toDouble(), args.startLng.toDouble())
        map.addMarker(markerUserStart)
        map.addMarker(markerUserDestination)
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

        drawRoute()
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

    private fun cameraUpdate(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
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

    private fun drawRoute() {
        val now = DateTime()
        try {
            directionsResult = DirectionsApi.newRequest(getGeoContext(requireActivity()))
                .mode(TravelMode.DRIVING)
                .origin(
                    com.google.maps.model.LatLng(
                        args.startLat.toDouble(),
                        args.startLng.toDouble()
                    )
                )
                .destination(
                    com.google.maps.model.LatLng(
                        args.endLat.toDouble(),
                        args.endLng.toDouble()
                    )
                )
                .departureTime(now)
                .await()
            val handler = Handler(Looper.getMainLooper())
            directionsResult.let {
                handler.post {
                    showRoute(directionsResult)
                }
            }
        } catch (e: ApiException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showRoute(directionsResult: DirectionsResult) {
        val decodedPath: List<LatLng> =
            decodePoly(directionsResult.routes[0].overviewPolyline.encodedPath)
        map.addPolyline(
            PolylineOptions()
                .color(getColor(requireActivity().resources, R.color.color_green, null))
                .width(20f)
                .addAll(decodedPath)
        )
    }

    private fun decodePoly(encodedPath: String?): List<LatLng> {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        var lat = 0
        var lng = 0

        encodedPath?.let {
            while (index < it.length) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = it.codePointAt(index++) - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = it.codePointAt(index++) - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val p = LatLng(
                    lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5
                )
                poly.add(p)
            }
        }

        return poly

    }

    private fun getGeoContext(mContext: Context): GeoApiContext {
        val geoApiContext = GeoApiContext()
        return geoApiContext.setQueryRateLimit(3)
            .setApiKey(mContext.getString(R.string.google_map_api_key))
            .setConnectTimeout(1, TimeUnit.SECONDS)
            .setReadTimeout(1, TimeUnit.SECONDS)
            .setWriteTimeout(1, TimeUnit.SECONDS)
    }
}
