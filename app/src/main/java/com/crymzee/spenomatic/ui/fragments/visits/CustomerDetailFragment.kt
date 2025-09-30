package com.crymzee.spenomatic.ui.fragments.visits

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.crymzee.drivetalk.utils.FileUtils.saveImageToFile
import com.crymzee.spenomatic.BuildConfig
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.adapter.RecentVisitAdapter
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.DialogCheckOutBinding
import com.crymzee.spenomatic.databinding.FragmentCustomerDetailBinding
import com.crymzee.spenomatic.enums.SelectionType
import com.crymzee.spenomatic.model.request.checkInVisit.CheckInVisitRequest
import com.crymzee.spenomatic.model.request.checkInVisit.Location
import com.crymzee.spenomatic.model.response.visitDetail.Data
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.hide
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.utils.showToast
import com.crymzee.spenomatic.utils.toCamelCase
import com.crymzee.spenomatic.utils.visible
import com.crymzee.spenomatic.viewModel.VisitsViewModel
import com.example.flowit.abstracts.PaginationScrollListener
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerDetailFragment : BaseFragment() {
    private lateinit var binding: FragmentCustomerDetailBinding
    private lateinit var leaveListAdapter: RecentVisitAdapter
    private var activeDialog: AlertDialog? = null  // Keep track of the active dialog
    private val visitViewModel: VisitsViewModel by activityViewModels()
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private val layoutManager by lazy { getLinearLayoutManager() }
    private var visitId = 0
    private var customerId = 0
    private var selectedCoverImageUri: Uri? = null
    private var currentSelectionType: SelectionType? = null
    private var photoUri: Uri? = null

    private var checkoutDialogBinding: DialogCheckOutBinding? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let {
                    val compressedFile = saveImageToFile(it) // Compress & save image
                    selectedCoverImageUri = Uri.fromFile(compressedFile)
                    visitViewModel.filePathProfile = compressedFile?.absolutePath
                    updateDialogImage()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_customer_detail, container, false
            )
            viewInit()
            hideBottomNav()
        }


        return binding.root
    }

    private fun viewInit() {
        val isVisited = arguments?.getBoolean("isVisited") ?: false
        visitId = arguments?.getInt("visitId") ?: 0
        customerId = arguments?.getInt("customerId") ?: 0
        visitDetail(visitId)
        initAdapter()
        binding.apply {
            ivBack.setOnClickListener { goBack() }


        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visitViewModel.page = 1
        visitViewModel.perPage = 10
        observePublicPosts()

        // Only load on first time
        if (leaveListAdapter.itemCount == 0) {
            visitViewModel.getRecentVisit(customerId)
        }
    }


    fun checkoutPopUp() {
        try {
            activeDialog?.dismiss()  // Dismiss any existing dialog before showing a new one

            // Inflate layout using ViewBinding
            val binding = DialogCheckOutBinding.inflate(LayoutInflater.from(context))
            checkoutDialogBinding = binding
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Store reference to active dialog
            activeDialog = alertDialog

            updateDialogImage()


            // OK button with slight delay before invoking the callback
            binding.ivCancel.setOnClickListener {
                visitViewModel.filePathProfile = null
                selectedCoverImageUri = null
                alertDialog.dismiss()
                activeDialog = null
            }

            binding.ivDelete.setOnClickListener {
                binding.apply {
                    containerImages.hide()
                    ivUpload.visible()
                    tvUpload.visible()
                    ivImage.setImageBitmap(null)
                    visitViewModel.filePathProfile = null
                    selectedCoverImageUri = null
                }


            }


            // OK button with slight delay before invoking the callback
            binding.layoutAddPhoto.setOnClickListener {
                checkCameraPermissionAndCapture()
            }
            binding.btnPost.setOnClickListener {
                val description = binding.etDescription.text.toString()
                visitViewModel.visitSummary = description
                visitViewModel.checkOutTime = getLocalDateTime()
                visitViewModel.status = "visited"
                if (visitViewModel.filePathProfile.isNullOrEmpty() || visitViewModel.filePathProfile == "") {
                    showErrorPopup(requireContext(), "", "Please upload your visit location image")
                } else if (visitViewModel.visitSummary == "") {
                    showErrorPopup(requireContext(), "", "Please enter your visit summary")
                } else {
                    checkOutVisit(alertDialog)
                }




                Handler(Looper.getMainLooper()).postDelayed({
//                    reportUser(description, post.id)
                }, 300)
            }

            // Set the view and show dialog
            alertDialog.setView(binding.root)
            alertDialog.setCancelable(false)
            alertDialog.show()

        } catch (e: Exception) {
            Log.e("CustomDialog", "Error showing dialog: $e")
        }
    }

    private fun updateDialogImage() {
        checkoutDialogBinding?.let { binding ->
            selectedCoverImageUri?.let { uri ->
                binding.ivImage.setImageURI(uri)
                binding.containerImages.visible()
                binding.ivUpload.hide()
                binding.tvUpload.hide()
            } ?: run {
                binding.containerImages.hide()
                binding.ivUpload.visible()
                binding.tvUpload.visible()
            }
        }
    }

    private fun visitDetail(visitId: Int) {
        visitViewModel.getVisitDetail(visitId).observe(viewLifecycleOwner) { response ->
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

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    response.data?.data?.let { data ->
                        bindData(data)

                    }

                }
            }
        }
    }

    private fun bindData(data: Data) {
        binding.apply {
            labelHello.text = data.customer.fullname
            labelExplore.text = data.customer.industry_type

            tvLastVisit.text = data.customer.industry_type

            tvAddress.text = data.customer.address
            tvTimeDate.text = data.schedule_date
            tvEmail.text = data.customer.contacts.first().email
            tvPhone.text = data.customer.contacts.first().phone
            tvFrequency.text = data.customer.visit_frequency.toCamelCase()
            tvRemark.text = data.remarks
            tvTotalVisit.text = data.customer.number_of_visits.toString()
            tvLastVisit.text = data.customer.latest_visit_date.toString()

            when (data.type) {
                "sales" -> {
                    ivNotifications.setImageResource(R.drawable.type_sales)
                }

                "service" -> {
                    ivNotifications.setImageResource(R.drawable.types_service)
                }
            }

            btnSave.setOnClickListener {
                if (data.status == "checked_in") {
                    checkoutPopUp()
                } else if (data.status == "pending") {
                    getCurrentLocation { lat, lng ->
                        val model = CheckInVisitRequest(
                            getLocalDateTime(), location = Location(
                                type = "Point", coordinates = listOf(lng, lat)
                            ), schedule_date = getLocalDate(), status = "checked_in"
                        )

                        // Send to API
                        SpenoMaticLogger.logErrorMsg("CheckIn Payload", model.toString())
                        checkInVisit(visitId, model)
                    }
                }

            }

            when (data.status) {
                "checked_in" -> {
                    btnSave.text = "Check Out"

                }

                "pending" -> {
                    btnSave.text = "Check In"
                }

                "visited" -> {
                    btnSave.hide()
                    btnVisited.visible()
                }
            }

        }
    }

    private fun checkInVisit(visitId: Int, model: CheckInVisitRequest) {
        visitViewModel.checkInVisit(visitId, model).observe(viewLifecycleOwner) { response ->
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

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    binding.btnSave.text = "Check Out"
                    visitDetail(visitId)
                    visitViewModel.page = 1
                    visitViewModel.getRecentVisit(customerId)

                }
            }
        }
    }

    private fun observePublicPosts() {
        visitViewModel.getAllRecentVisitLiveData.removeObservers(viewLifecycleOwner)
        visitViewModel.getAllRecentVisitLiveData.observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading<*>

            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                }

                is Resource.Loading -> {}

                is Resource.Success -> {

                    response.data?.let { posts ->
                        val isFirstPage = visitViewModel.page == 1
                        val isEmptyList = posts.data.isEmpty() && visitViewModel.page == 1
                        binding.tvNoData.isVisible = isEmptyList
                        binding.tvAvgVisit.text ="${posts.stats.avg_visit_time} min"


                        if (isFirstPage) {
                            leaveListAdapter.clearList(posts.data)
                        } else {
                            leaveListAdapter.addPaginatedDataToList(posts.data)
                        }

                        val hasNextPage = !posts.pagination.links.next.isNullOrEmpty()
                        isLastPage = !hasNextPage
                        isLoading = false

                        if (hasNextPage) {
                            visitViewModel.page++
                        }
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        leaveListAdapter = RecentVisitAdapter(requireContext())
        binding.rvSchedule.apply {
            adapter = leaveListAdapter
            layoutManager = this@CustomerDetailFragment.layoutManager
            addOnScrollListener(object :
                PaginationScrollListener(this@CustomerDetailFragment.layoutManager, 10) {
                override fun isLastPage(): Boolean = this@CustomerDetailFragment.isLastPage
                override fun isLoading(): Boolean = this@CustomerDetailFragment.isLoading

                override fun loadMoreItems() {
                    if (!isLastPage && !isLoading) {
                        isLoading = true
                        visitViewModel.getRecentVisit(customerId)
                    }
                }
            })
        }

    }

    private fun checkOutVisit(alertDialog: AlertDialog) {
        visitViewModel.checkOutVisit(visitId).observe(viewLifecycleOwner) { response ->
            binding.loader.isVisible = response is Resource.Loading
            when (response) {
                is Resource.Error -> {
                    val errorMessage = extractFirstErrorMessage(response.throwable)
                    SpenoMaticLogger.logErrorMsg(errorMessage.heading, errorMessage.description)
                    showErrorPopup(
                        requireContext(), errorMessage.heading, errorMessage.description
                    )
                }

                is Resource.Loading -> {
                }

                is Resource.Success -> {
                    alertDialog.dismiss()
                    activeDialog = null
                    visitViewModel.filePathProfile = null
                    selectedCoverImageUri = null
                    showSuccessPopup(
                        requireContext(),
                        "Success!",
                        "Visit has been checked out successfully",
                        onConfirm = {

                            goBack()
                        })


                }
            }

        }
    }

    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            captureImageWithCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
        }
    }

    private fun captureImageWithCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(), "${BuildConfig.APPLICATION_ID}.fileprovider", photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showToast("Camera not available", Toast.LENGTH_SHORT)
        } catch (e: IOException) {
            showToast("Error creating file", Toast.LENGTH_SHORT)
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun getCurrentLocation(onLocationReady: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO request runtime permission before using
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location.latitude, location.longitude)
            }
        }
    }

    private fun getLocalDate(): String {
        val calendar = Calendar.getInstance() // local time
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun getLocalDateTime(): String {
        val calendar = Calendar.getInstance() // local time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(calendar.time)
    }

}
