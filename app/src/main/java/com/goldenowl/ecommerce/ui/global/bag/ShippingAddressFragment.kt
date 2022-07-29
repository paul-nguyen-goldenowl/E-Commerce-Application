package com.goldenowl.ecommerce.ui.global.bag

import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.adapter.ShippingAddressAdapter
import com.goldenowl.ecommerce.databinding.FragmentShippingAddressesBinding
import com.goldenowl.ecommerce.models.data.Address
import com.goldenowl.ecommerce.ui.global.BaseHomeFragment
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.Constants

class ShippingAddressFragment : BaseHomeFragment<FragmentShippingAddressesBinding>(),
    ShippingAddressAdapter.ICheckListener {

    private var listAddress: List<Address> = emptyList()
    private var defaultAddress = 0
    private lateinit var adapter: ShippingAddressAdapter

    override fun getViewBinding(): FragmentShippingAddressesBinding {
        return FragmentShippingAddressesBinding.inflate(layoutInflater)
    }

    override fun setObservers() {
        viewModel.listAddress.observe(viewLifecycleOwner) {
            listAddress = it
            adapter.setData(listAddress, defaultAddress)
            if (it.isEmpty()) {
                binding.tvYourAddress.visibility = View.VISIBLE
            } else {
                binding.tvYourAddress.visibility = View.GONE
            }
        }

        viewModel.defaultAddressIndex.observe(viewLifecycleOwner) {
            if (it != null) {
                defaultAddress = it
                adapter.setData(listAddress, defaultAddress)
            }
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner) {
            when (it) {
                BaseLoadingStatus.LOADING -> binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
                else -> binding.layoutLoading.loadingFrameLayout.visibility = View.INVISIBLE
            }
        }
    }


    override fun setViews() {

        adapter = ShippingAddressAdapter(this)
        adapter.setData(listAddress, defaultAddress)
        binding.rcvAddress.adapter = adapter
        adapter.setData(listAddress, defaultAddress)

        binding.ivAddAddress.setOnClickListener {
            findNavController().navigate(R.id.add_address_dest)
        }
    }

    companion object {
        const val TAG = "BagFragment"
    }

    override fun setAppbar() {
        binding.topAppBar.collapsingToolbarLayout.title = getString(R.string.shipping_address)
        binding.topAppBar.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun selectAddress(position: Int) {
        viewModel.setDefaultAddress(position)
    }

    override fun removeAddress(position: Int) {
        viewModel.removeAddress(position)
    }

    override fun insertAddress(address: Address) {
        viewModel.insertAddress(address)
    }

    override fun editAddress(position: Int) {
        val address = listAddress[position]
        findNavController().navigate(
            R.id.add_address_dest,
            bundleOf(Constants.KEY_ADDRESS to address, Constants.KEY_POSITION to position)
        )
    }
}