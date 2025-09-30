package com.crymzee.spenomatic.ui.fragments.customer

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.PlaceAutoSuggestAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentMapBinding
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.bitmapDescriptorFromVector
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.goBackWithResult
import com.crymzee.spenomatic.viewModel.CustomersViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.Locale
import kotlin.math.abs

class MapFragment : BaseFragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    private var selectedAddress: String? = null
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null
    var customerId = 0
    var lat = 0.0
    var lng = 0.0
    var address = ""
    var isEditable = false

    private val customersViewModel: CustomersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        viewInit()

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun viewInit() {
        val adapter =
            PlaceAutoSuggestAdapter(requireContext(), R.layout.simple_layout_places_suggession)

        binding.apply {
            customerId = arguments?.getInt("customerId") ?: 0
            isEditable = arguments?.getBoolean("isEditable") ?: false
            address = arguments?.getString("address") ?: ""
            lat = arguments?.getDouble("lat") ?: 0.0
            lng = arguments?.getDouble("lng") ?: 0.0



            ivBack.setOnClickListener {
                customersViewModel.address = selectedAddress.toString()
                customersViewModel.lat = selectedLat ?: 0.0
                customersViewModel.lng = selectedLng ?: 0.0
                goBack()
            }
            btnSave.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("address", customersViewModel.address)
                bundle.putDouble("lat", customersViewModel.lat ?: 0.0)
                bundle.putDouble("lng", customersViewModel.lng ?: 0.0)
                bundle.putInt("customerId", customerId)
                bundle.putBoolean("isEditable", isEditable)
                bundle.putBoolean("isFromMap", true)

//                customersViewModel.address  = selectedAddress.toString()
//                customersViewModel.lat  = selectedLat ?:0.0
//                customersViewModel.lng  = selectedLng ?: 0.0
                goBackWithResult("mapData", bundle)

            }
            etAddress.setAdapter(adapter)
            etAddress.dropDownVerticalOffset = 0
            etAddress.dropDownAnchor = etAddress.id

            etAddress.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, _, _ ->

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocationName(etAddress.text.toString(), 1)

                        if (!addresses.isNullOrEmpty()) {
                            val selectedAddress = addresses[0]
                            val streetAddress = selectedAddress.getAddressLine(0)
                            val latitude = selectedAddress.latitude
                            val longitude = selectedAddress.longitude
                            customersViewModel.address = streetAddress.toString()
                            customersViewModel.lat = latitude
                            customersViewModel.lng = longitude

                            val latLng = LatLng(latitude, longitude)
                            moveMarker(latLng, streetAddress)
                        } else {
                            SpenoMaticLogger.logErrorMsg("Address", "No matching address found")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        SpenoMaticLogger.logErrorMsg(
                            "Geocoder Error",
                            "Unable to get street address"
                        )
                    }
                }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val defaultLocation = LatLng(31.5204, 74.3587) // Lahore

        when {
            // Case 1: Editing existing customer
            isEditable && lat != 0.0 && lng != 0.0 -> {
                val editLatLng = LatLng(lng, lat)

                if (address.isNotEmpty()) {
                    binding.etAddress.setText(address)
                    moveMarker(editLatLng, address) // ✅ handles camera
                } else {
                    updateAddressFromLatLng(editLatLng)
                }

                customersViewModel.address = address
                customersViewModel.lat = lat
                customersViewModel.lng = lng
            }

            // Case 2: New customer but lat/lng provided
            lat != 0.0 && lng != 0.0 -> {
                val editLatLng = LatLng(lat, lng)

                if (address.isNotEmpty()) {
                    binding.etAddress.setText(address)
                    moveMarker(editLatLng, address)
                } else {
                    updateAddressFromLatLng(editLatLng)
                }

                customersViewModel.address = address
                customersViewModel.lat = lat
                customersViewModel.lng = lng
            }

            // Case 3: Default location
            else -> {
                moveMarker(defaultLocation, "Drag to adjust location")
            }
        }

        // ✅ Marker drag listener
        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                updateAddressFromLatLng(marker.position)
            }
        })

        // ✅ Map click listener
        googleMap.setOnMapClickListener { latLng ->
            updateAddressFromLatLng(latLng)
        }
    }






    private fun moveMarker(latLng: LatLng, title: String) {
        Log.d("MoveMarker", "Requested move to: $latLng")

        googleMap.clear()
        currentMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(true)
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_marker))
        )

        googleMap.setOnMapLoadedCallback {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }


    private fun updateAddressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val formattedLatLng = formatLatLng(latLng.latitude, latLng.longitude)

            if (!addresses.isNullOrEmpty()) {
                val newAddress = addresses[0].getAddressLine(0)
                binding.etAddress.setText(newAddress)       // ✅ correct
                moveMarker(latLng, newAddress)

                customersViewModel.address = newAddress
                customersViewModel.lat = latLng.latitude
                customersViewModel.lng = latLng.longitude
            } else {
                binding.etAddress.setText(formattedLatLng)  // ✅ fallback
                moveMarker(latLng, formattedLatLng)

                customersViewModel.address = formattedLatLng
                customersViewModel.lat = latLng.latitude
                customersViewModel.lng = latLng.longitude
            }
        } catch (e: IOException) {
            e.printStackTrace()
            SpenoMaticLogger.logErrorMsg("Geocoder Error", "Unable to get address")
        }
    }



    private fun formatLatLng(lat: Double, lng: Double): String {
        val latSuffix = if (lat >= 0) "N" else "S"
        val lngSuffix = if (lng >= 0) "E" else "W"
        return String.format(
            "%.4f° %s, %.4f° %s",
            abs(lat), latSuffix,
            abs(lng), lngSuffix
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
