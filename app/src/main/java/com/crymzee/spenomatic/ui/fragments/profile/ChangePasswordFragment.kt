package com.crymzee.spenomatic.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentUpdatePasswordBinding
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.AuthViewModel

class ChangePasswordFragment : BaseFragment() {
    private lateinit var binding: FragmentUpdatePasswordBinding
    var oldPassword = ""
    var newPassword = ""
    var confirmPassword = ""

    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_update_password,
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
            btnSave.setOnClickListener {
                authViewModel.firstName = null
                authViewModel.email = null
                checkValidations()
            }
        }
    }

    private fun checkValidations() {
        oldPassword = binding.etPassword.text.toString()
        newPassword = binding.etNewPassword.text.toString()
        confirmPassword = binding.etConfirmPassword.text.toString()

        val validationResult =
            authViewModel.validateUpdatePassword(oldPassword, newPassword, confirmPassword)
        if (validationResult.first) {
            authViewModel.newPassword = newPassword
            authViewModel.currentPassword = oldPassword
            authViewModel.confirmPassword = confirmPassword
            updatePassword()

        } else {
            val message = getString(validationResult.second)
            showErrorPopup(requireContext(), description = message)
        }
    }

    private fun updatePassword() {
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
                        showSuccessPopup(
                            requireContext(),
                            "Success!", "Password has been update successfully",
                            onConfirm = {
                                goBack()
                            })


                    }
                }

            }
    }
}