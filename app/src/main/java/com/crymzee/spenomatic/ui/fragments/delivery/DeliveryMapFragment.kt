package com.crymzee.spenomatic.ui.fragments.delivery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentMapDeliveryBinding
import com.crymzee.spenomatic.model.request.UpdateDeliveryRequest
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.bitmapDescriptorFromVector
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.parseLocation
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.VisitsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveryMapFragment : BaseFragment(), OnMapReadyCallback {

    private var _binding: FragmentMapDeliveryBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null

    var deliveryId = 0
    var lat = 0.0
    var lng = 0.0

    private val visitViewModel: VisitsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapDeliveryBinding.inflate(inflater, container, false)

        viewInit()

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun viewInit() {


        binding.apply {
            deliveryId = arguments?.getInt("deliveryId") ?: 0
            val location = arguments?.getString("location") ?: 0
            val result = parseLocation(location.toString())
            if (result != null) {
                val (setlng, setlat) = result
                lng = setlat
                lat = setlng

            }


            ivBack.setOnClickListener {
                goBack()
            }
            btnSave.setOnClickListener {
                updateDelivery()

            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val defaultLocation = LatLng(31.5204, 74.3587) // Lahore

        when {

            // Case 2: New customer but lat/lng provided
            lat != 0.0 && lng != 0.0 -> {
                val editLatLng = LatLng(lat, lng)

                moveMarker(editLatLng, "")
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
            }
        })

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun updateDelivery() {
        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        visitViewModel.updateDelivery(
            deliveryId,
            updateDeliveryRequest = UpdateDeliveryRequest(
                delivery_datetime = currentDateTime,
                status = "delivered"
            )
        ).observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading
            when (response) {

                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                    showErrorPopup(
                        requireContext(),
                        heading = errorMessage.heading,
                        description = errorMessage.description
                    )
                }

                is Resource.Loading -> Unit

                is Resource.Success -> {
                    response.data?.let {
                        showSuccessPopup(
                            requireContext(),
                            "Success!", "Delivery has been done successfully",
                            onConfirm = {
                                navigateClear(R.id.action_deliveryMapFragment_to_deliveryFragment)
                            }
                        )
                    }
                }
            }
        }
    }

}
