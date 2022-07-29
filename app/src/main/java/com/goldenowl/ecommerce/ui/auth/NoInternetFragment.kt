package com.goldenowl.ecommerce.ui.auth

import com.goldenowl.ecommerce.databinding.FragmentNoInternetBinding
import com.goldenowl.ecommerce.ui.global.BaseFragment
import com.goldenowl.ecommerce.utils.Utils


class NoInternetFragment : BaseFragment<FragmentNoInternetBinding>() {


    override fun getViewBinding(): FragmentNoInternetBinding {
        return FragmentNoInternetBinding.inflate(layoutInflater)
    }

    override fun setViews() {
        binding.btnGoBack.setOnClickListener {
            Utils.launchHome(requireContext())
            activity?.finish()
        }
    }


    override fun setObservers() {
    }

    override fun setAppbar() {
    }
}