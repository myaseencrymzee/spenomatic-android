package com.crymzee.spenomatic.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentProfileBinding
import com.crymzee.spenomatic.model.request.createOutsideExpense.Visit
import com.crymzee.spenomatic.model.response.meResponse.MeResponseBody
import com.crymzee.spenomatic.sharedPreference.SharedPrefsHelper
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.ui.auth.SignInActivity
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.confirmationPopUp
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.viewModel.AuthViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.collections.set

class ProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileBinding
    private val authViewModel: AuthViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_profile,
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
            containerPersonalInfo.setOnClickListener {
                navigate(R.id.action_profile_fragment_to_update_profile_fragment)
            }
            containerChangePasword.setOnClickListener {
                navigate(R.id.action_profile_fragment_to_update_password_fragment)
            }
            btnSave.setOnClickListener {

                confirmationPopUp(
                    requireContext(),
                    heading = "Confirm Logout",
                    description = "Are you sure you want to log out? You will need to sign in again to continue.",
                    icon = R.drawable.ic_logout,
                    onConfirm = {
                        SharedPrefsHelper.clearAllData()
                        val intent = Intent(requireContext(), SignInActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                )

            }

            btnDelete.setOnClickListener {

                confirmationPopUp(
                    requireContext(),
                    heading = "Delete Confirm",
                    description = "Are you sure you want to delete this account? You will no longer be able to sign in again",
                    icon = R.drawable.ic_delete_services,
                    onConfirm = {
                        SharedPrefsHelper.clearAllData()
                        val intent = Intent(requireContext(), SignInActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                )

            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }

    override fun onResume() {
        super.onResume()
        authViewModel.getUserProfile()
    }

    private fun getData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.getUserProfile.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Success -> bindData(response.data)
                        is Resource.Error -> {
                            val httpCode = (response.throwable as? HttpException)?.code()
                            val errorMessage = extractFirstErrorMessage(response.throwable)
                            SpenoMaticLogger.logErrorMsg("Error", errorMessage.description)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun bindData(data: MeResponseBody?) {
        binding.apply {
            SharedPrefsHelper.setUserEmail(data?.email)
            SharedPrefsHelper.setPhoneNumber(data?.phone)
            SharedPrefsHelper.setUserID(data?.id)
            SharedPrefsHelper.setStatus(data?.status)
            SharedPrefsHelper.setName(data?.fullname)
            SharedPrefsHelper.setUserImage(data?.profile_picture)


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
                .into(binding.containerProfileImage)

            tvName.text = data?.fullname.toString()


        }
    }
}