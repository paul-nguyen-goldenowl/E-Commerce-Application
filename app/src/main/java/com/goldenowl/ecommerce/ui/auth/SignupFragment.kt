package com.goldenowl.ecommerce.ui.auth

import android.view.View
import androidx.navigation.Navigation
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentSignupBinding
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.FieldValidators
import com.goldenowl.ecommerce.utils.Utils.hideKeyboard
import com.goldenowl.ecommerce.utils.Utils.launchHome
import com.google.android.material.textfield.TextInputLayout


class SignupFragment : BaseAuthFragment<FragmentSignupBinding>() {

    override fun setAppBar() {
        binding.topAppBar.toolbar.title = getString(R.string.signup)
    }

    override fun setObservers() {
        super.setObservers()
        textInputViewModel.errorSignUpEmail.observe(viewLifecycleOwner) { errorEmail ->
            validEmail(errorEmail)
        }
        textInputViewModel.errorSignUpPassword.observe(viewLifecycleOwner) { errorPassword ->
            validPassword(errorPassword)
        }

        textInputViewModel.signUpFormValid.observe(viewLifecycleOwner) { signUpValid ->
            binding.btnSignup.isEnabled = signUpValid
        }

        viewModel.signUpStatus.observe(viewLifecycleOwner) {
            handleSignUp(it)
        }

        viewModel.logInStatus.observe(viewLifecycleOwner) {
            handleLogin(it)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            hasError(it)
        }
        viewModel.restoreStatus.observe(viewLifecycleOwner) {
            validRestore(it)
        }
    }

    private fun handleLogin(it: BaseLoadingStatus?) {
        when (it) {
            BaseLoadingStatus.LOADING -> {
                setLoading(true)
                binding.btnSignup.isEnabled = false
            }
            BaseLoadingStatus.SUCCEEDED -> {
                setLoading(false)
                viewModel.restoreUserData()
            }
            BaseLoadingStatus.FAILED -> {
                setLoading(false)
                binding.btnSignup.isEnabled = true
            }
        }
    }

    private fun hasError(it: String?) {
        if (it != null) {
            if (it.isNotEmpty()) {
                binding.inputLayoutEmail.error = it
            }
        }
    }

    private fun handleSignUp(signUpStatus: BaseLoadingStatus) {
        when (signUpStatus) {
            BaseLoadingStatus.LOADING -> {
                setLoading(true)
                binding.btnSignup.isEnabled = false
            }
            BaseLoadingStatus.SUCCEEDED -> {
                setLoading(false)
                launchHome(requireContext())
                activity?.finish()
            }
            BaseLoadingStatus.FAILED -> {
                setLoading(false)
                binding.btnSignup.isEnabled = true
            }
            else -> setLoading(false)
        }
    }

    private fun validPassword(errorPassword: String?) {
        with(binding) {
            if (errorPassword.isNullOrEmpty()) {
                inputLayoutPassword.isErrorEnabled = false
            } else {
                inputLayoutPassword.error = errorPassword
                inputLayoutPassword.errorIconDrawable = null
            }
        }
    }

    private fun validEmail(errorEmail: String?) {
        with(binding) {

            if (errorEmail.isNullOrEmpty()) {
                inputLayoutEmail.isErrorEnabled = false
                inputLayoutEmail.endIconMode = TextInputLayout.END_ICON_CUSTOM
            } else {
                inputLayoutEmail.error = errorEmail
            }
        }
    }

    override fun setupListeners() {
        with(binding) {
            edtEmail.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkEmail(edtEmail.text.toString(), 1)
                    textInputViewModel.setSignUpFormValid()
                }

            })

            edtPassword.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkPassword(edtPassword.text.toString(), 1)
                    textInputViewModel.setSignUpFormValid()
                }
            })

            edtName.addTextChangedListener(object : FieldValidators.TextChange {
                override fun onTextChanged(s: CharSequence?) {
                    textInputViewModel.checkName(edtName.text.toString())
                    textInputViewModel.setSignUpFormValid()
                }
            })

        }
    }

    override fun setViews() {
        with(binding) {
            btnSignup.setOnClickListener {
                hideKeyboard()
                viewModel.signUpWithEmail(
                    edtEmail.text.toString(),
                    edtPassword.text.toString(),
                    edtName.text.toString()
                )
            }

            layoutAlreadyHasAcc.setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.login_dest)
            )

            ivFacebook.setOnClickListener {
                loginWithFacebook()
            }
            ivGoogle.setOnClickListener { viewModel.logInWithGoogle(this@SignupFragment) }
        }

    }

    override fun setLoading(isShow: Boolean) {
        if (isShow) {
            binding.layoutLoading.loadingFrameLayout.visibility = View.VISIBLE
        } else {
            binding.layoutLoading.loadingFrameLayout.visibility = View.GONE
        }
    }

    override fun getViewBinding(): FragmentSignupBinding {
        return FragmentSignupBinding.inflate(layoutInflater)
    }
}