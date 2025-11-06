package com.crymzee.spenomatic.ui.fragments.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crymzee.drivetalk.utils.FileUtils.saveImageToFile
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentUpdateProfileBinding
import com.crymzee.spenomatic.enums.SelectionType
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.AuthViewModel

class UpdateProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentUpdateProfileBinding
    var photo = ""
    var name = ""
    private val authViewModel: AuthViewModel by activityViewModels()
    private var selectedCoverImageUri: Uri? = null
    private var currentSelectionType: SelectionType? = null


    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    val compressedFile = saveImageToFile(imageUri) // Compress & save image
                    when (currentSelectionType) {

                        SelectionType.COVER -> {
                            selectedCoverImageUri = Uri.fromFile(compressedFile)
                            authViewModel.filePathProfile = compressedFile?.absolutePath
                            updateButtonState()
                            updateProfile()
                        }

                        else -> {}
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_update_profile,
                container,
                false
            )
            viewInit()
            hideBottomNav()
        }


        return binding.root
    }

    private fun viewInit() {
        binding.apply {
            ivBack.setOnClickListener { goBack() }
            etName.setText(SharedPrefsHelper.getUserName())
            etEmail.setText(SharedPrefsHelper.getUserEmail())
            Glide.with(requireContext()).load(SharedPrefsHelper.getUserImage()).into(ivProfile)

            ivEdit.setOnClickListener {
                openGallery(SelectionType.COVER)
            }
            btnSave.setOnClickListener {
                authViewModel.password = null
                authViewModel.confirmPassword = null
                authViewModel.newPassword = null
                authViewModel.firstName = etName.text.toString()
                if (authViewModel.firstName.isNullOrEmpty()) {
                    showErrorPopup(requireContext(), description = "Enter the your name")

                } else {
                    updateProfile()
                }
            }
        }

    }

    private fun updateButtonState() {
        val currentDescription = binding.etName.text?.toString()?.trim().orEmpty()
        val hasDescriptionChanged = currentDescription != name

        val enable = hasDescriptionChanged

        binding.btnSave.isEnabled = enable
        binding.btnSave.alpha = if (enable) 1f else 0.5f
    }

    private fun openGallery(selectionType: SelectionType) {
        currentSelectionType = selectionType
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // Only allow image files to be selected
        }
        imagePickerLauncher.launch(intent)
    }

    private fun updateProfile() {
        authViewModel.updateProfile()
            .observe(viewLifecycleOwner) { response ->
                binding.loader.isVisible = response is Resource.Loading
                when (response) {
                    is Resource.Error -> {
                        val errorMessage = extractFirstErrorMessage(response.throwable)
                        SpenoMaticLogger.logErrorMsg(errorMessage.heading, errorMessage.description)
                        showErrorPopup(
                            requireContext(),
                            errorMessage.heading,
                            errorMessage.description
                        )
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        val data = response.data

                        binding.apply {


                            val circularProgressDrawable = CircularProgressDrawable(requireContext()).apply {
                                strokeWidth = 5f      // thickness of the spinner
                                centerRadius = 30f    // size of the spinner
                                setColorSchemeColors(requireContext().getColor(R.color.theme_blue)) // optional color
                                start()
                            }
                            Glide.with(requireContext())
                                .load(data?.profile_picture ?: "")
                                .placeholder(circularProgressDrawable) // show spinner while loading
                                .error(R.drawable.dummy_image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(ivProfile)

                        }
                        SharedPrefsHelper.setUserImage(data?.profile_picture ?: "")
                        SharedPrefsHelper.setName(data?.fullname ?: "")
                        SharedPrefsHelper.setUserEmail(data?.email ?: "")

                        showSuccessPopup(
                            requireContext(),
                            "Success!", "Profile has been update successfully",
                            onConfirm = {
                                goBack()
                            })


                    }
                }

            }
    }


}