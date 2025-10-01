package com.crymzee.spenomatic.ui.fragments.expenses.addOthers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.crymzee.spenomatic.R
import com.crymzee.spenomatic.base.BaseFragment
import com.crymzee.spenomatic.databinding.FragmentAddOthersBinding
import com.crymzee.spenomatic.model.request.OtherExpensesRequest
import com.crymzee.spenomatic.state.Resource
import com.crymzee.spenomatic.utils.SpenoMaticLogger
import com.crymzee.spenomatic.utils.extractFirstErrorMessage
import com.crymzee.spenomatic.utils.goBack
import com.crymzee.spenomatic.utils.showErrorPopup
import com.crymzee.spenomatic.utils.showSuccessPopup
import com.crymzee.spenomatic.viewModel.ExpensesViewModel

class AddOthersFragment : BaseFragment() {
    private lateinit var binding: FragmentAddOthersBinding
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (!::binding.isInitialized) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_add_others,
                container,
                false
            )
            hideBottomNav()
            viewInit()

        }


        return binding.root
    }

    private fun viewInit() {
        binding.apply {
            ivBack.setOnClickListener { goBack() }
            btnSave.setOnClickListener {
                val description = etDescription.text.toString()
                val price = etAmount.text.toString()

                if (description.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Description field cannot be empty")
                } else if (price.isEmpty()) {
                    showErrorPopup(requireContext(), "", "Please enter amount")
                }else if (price == "0") {
                    showErrorPopup(requireContext(), "", "Amount must be at least 1")
                } else {
                    val body = OtherExpensesRequest(
                        price, description
                    )
                    addOtherExpense(body)
                }
            }
        }
    }


    private fun addOtherExpense(model: OtherExpensesRequest) {
        expensesViewModel.createOtherExpenses(model)
            .observe(viewLifecycleOwner) { response ->
                binding.loader.isVisible = response is Resource.Loading<*>

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

                    is Resource.Success -> {
                        showSuccessPopup(
                            requireContext(),
                            "Success!", "Expense has been created successfully",
                            onConfirm = {
                                navigateClear(R.id.action_addOthersFragment_to_expensesFragment)
                            })
                    }

                    is Resource.Loading<*> -> {}
                }
            }
    }
}