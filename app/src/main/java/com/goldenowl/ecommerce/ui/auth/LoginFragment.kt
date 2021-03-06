package com.goldenowl.ecommerce.ui.auth

import android.view.View
import androidx.navigation.fragment.findNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentLoginBinding
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.google.android.material.textfield.TextInputLayout


class LoginFragment : BaseAuthFragment<FragmentLoginBinding>() {

    override fun setAppBar() {
        binding.topAppBar.toolbar.title = getString(R.string.login)
        binding.topAppBar.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    override fun setObservers() {
        with(textInputViewModel) {
            errorLoginEmail.observe(viewLifecycleOwner) { errorEmail ->
                handleErrorEmail(errorEmail)
            }

            errorLoginPassword.observe(viewLifecycleOwner) { errorPassword ->
                handlePassword(errorPassword)
            }

            logInFormValid.observe(viewLifecycleOwner) { logInValid ->
                binding.btnLogin.isEnabled = logInValid
            }
        }

        with(viewModel) {
            logInStatus.observe(viewLifecycleOwner) { logInStatus ->
                validLogin(logInStatus)
            }
            errorMessage.observe(viewLifecycleOwner) {
                showErrorMessage(it)
            }
            restoreStatus.observe(viewLifecycleOwner) {
                validRestore(it)
            }
        }
    }

    override fun setLoading(isShow: Boolean) {
        if (isShow) {
            binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
        } else {
            binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
        }
    }

    private fun showErrorMessage(err: String?) {
        if (err == null)
            return
        if (err.isNotEmpty()) {
            binding.tvErr.text = err
            binding.tvErr.visibility = View.VISIBLE
        } else {
            binding.tvErr.visibility = View.GONE
        }
    }

    private fun validLogin(logInStatus: BaseLoadingStatus) {
        when (logInStatus) {
            BaseLoadingStatus.LOADING -> {
                setLoading(true)
                binding.btnLogin.isEnabled = false
            }
            BaseLoadingStatus.SUCCEEDED -> {
                setLoading(false)
                viewModel.restoreUserData()
            }
            BaseLoadingStatus.FAILED -> {
                setLoading(false)
            }
        }
    }

    private fun handlePassword(errorPassword: String?) {
        with(binding) {
            if (!errorPassword.isNullOrEmpty()) {
                inputLayoutPassword.error = errorPassword
                inputLayoutPassword.errorIconDrawable = null
            } else {
                inputLayoutPassword.isErrorEnabled = false
            }
        }
    }

    private fun handleErrorEmail(errorEmail: String?) {
        with(binding) {
            if (!errorEmail.isNullOrEmpty()) {
                inputLayoutEmail.error = errorEmail
                inputLayoutEmail
            } else {
                inputLayoutEmail.isErrorEnabled = false
                inputLayoutEmail.endIconMode = TextInputLayout.END_ICON_CUSTOM
            }
        }
    }

    override fun setupListeners() {
        with(binding) {
            edtEmail.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkEmail(edtEmail.text.toString(), 0)
                    textInputViewModel.setLoginFormValid()
                }
            })
            edtPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkPassword(edtPassword.text.toString(), 0)
                    textInputViewModel.setLoginFormValid()
                }
            })
            inputLayoutEmail.setOnFocusChangeListener { _, _ ->
                viewModel.emptyErrorMessage()
            }
            inputLayoutPassword.setOnFocusChangeListener { _, _ ->
                viewModel.emptyErrorMessage()
            }
        }
    }

    override fun setViews() {
        with(binding) {
            btnLogin.setOnClickListener {
                hideKeyboard()
                viewModel.logInWithEmail(
                    binding.edtEmail.text.toString().trim(),
                    binding.edtPassword.text.toString().trim()
                )
            }

            layoutForgotPassword.setOnClickListener {
                hideKeyboard()
                findNavController().navigate(R.id.forgot_dest)
            }

            ivFacebook.setOnClickListener {
                hideKeyboard()
                loginWithFacebook()
            }


            ivGoogle.setOnClickListener {
                hideKeyboard()
                viewModel.logInWithGoogle(this@LoginFragment)
            }
        }
    }


    override fun getViewBinding(): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(layoutInflater)
    }
}