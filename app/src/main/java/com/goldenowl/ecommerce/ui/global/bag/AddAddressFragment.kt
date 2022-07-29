package com.goldenowl.ecommerce.ui.global.bag

import android.view.View
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentAddAddressBinding
import com.goldenowl.ecommerce.models.data.Address
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants

class AddAddressFragment : BaseHomeFragment<FragmentAddAddressBinding>() {

    private var isEditing = false

    override fun getViewBinding(): com.goldenowl.ecommerce.databinding.FragmentAddAddressBinding {
        return FragmentAddAddressBinding.inflate(layoutInflater)
    }

    override fun setObservers() {
        viewModel.loadingStatus.observe(viewLifecycleOwner) {
            handleStatus(it)
        }
    }

    private fun handleStatus(status: BaseLoadingStatus?) {
        when (status) {
            BaseLoadingStatus.LOADING -> binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
            BaseLoadingStatus.SUCCEEDED -> {
                binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
                showToast(getString(R.string.success))
                findNavController().navigateUp()
            }
            else -> binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
        }
    }

    override fun init() {
        super.init()
        arguments.let {
            if (it != null) {
                isEditing = true
                restoreLayout(it.get(Constants.KEY_ADDRESS) as Address, it.get(Constants.KEY_POSITION) as Int)
            }
        }
    }

    private fun restoreLayout(address: Address, position: Int) {
        with(binding) {
            topAppBar.collapsingToolbarLayout.title = getString(R.string.editing_address)
            btnSaveAddress.text = getString(R.string.update_address)
            btnSaveAddress.setOnClickListener {
                val curAddress = viewModel.listAddress.value?.get(position)
                if (curAddress != null) {
                    curAddress.fullName = edtFullName.text.toString()
                    curAddress.address = edtAddress.text.toString()
                    curAddress.city = edtCity.text.toString()
                    curAddress.state = edtState.text.toString()
                    curAddress.zipCode = edtZipCode.text.toString()
                    curAddress.country = edtCountry.text.toString()
                    viewModel.updateAddress(curAddress, position)
                }
            }

            edtFullName.setText(address.fullName)
            edtAddress.setText(address.address)
            edtCity.setText(address.city)
            edtState.setText(address.state)
            edtZipCode.setText(address.zipCode)
            edtCountry.setText(address.country)
        }
    }

    override fun setViews() {
        if (!isEditing) {
            with(binding) {
                btnSaveAddress.setOnClickListener {
                    val newAddress = Address()
                    newAddress.fullName = edtFullName.text.toString()
                    newAddress.address = edtAddress.text.toString()
                    newAddress.city = edtCity.text.toString()
                    newAddress.state = edtState.text.toString()
                    newAddress.zipCode = edtZipCode.text.toString()
                    newAddress.country = edtCountry.text.toString()
                    viewModel.insertAddress(newAddress)
                }
            }
        }

        binding.edtCountry.setOnClickListener {
            val modalBottomSheet = BottomSheetSelectCountry(object : BottomSheetSelectCountry.ISelectCountry {
                override fun onSelectCountry(country: String) {
                    binding.edtCountry.setText(country)
                }

            })
            modalBottomSheet.enterTransition = View.GONE
            modalBottomSheet.show(parentFragmentManager, TAG)
        }
    }

    companion object {
        const val TAG = "AddAddressFragment"
    }

    override fun setAppbar() {
        if (!isEditing) {
            binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.adding_address)
        }
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}