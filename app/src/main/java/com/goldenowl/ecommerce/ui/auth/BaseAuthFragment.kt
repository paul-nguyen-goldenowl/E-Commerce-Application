package com.goldenowl.ecommerce.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.repo.LoginListener
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.ConnectionLiveData
import com.goldenowl.ecommerce.utils.MyResult
import com.goldenowl.ecommerce.utils.Utils
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.goldenowl.ecommerce.viewmodels.TextInputViewModel


abstract class BaseAuthFragment<VBiding : ViewBinding> : Fragment() {
    private val TAG = "BaseAuthFragment"

    lateinit var binding: VBiding
    lateinit var userManager: UserManager

    val textInputViewModel: TextInputViewModel by activityViewModels()
    val viewModel: AuthViewModel by activityViewModels()
    protected lateinit var connectionLiveData: ConnectionLiveData

    val facebookCallbackManager = CallbackManager.Factory.create() //facebook callback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        connectionLiveData = ConnectionLiveData(requireContext())
        userManager = UserManager.getInstance(requireContext())
        binding = getViewBinding()

        resetValue()
        setAppBar()
        setViews()
        setObservers()
        setupListeners()

        return binding.root
    }

    open fun setObservers() {
        connectionLiveData.observe(viewLifecycleOwner) {
            viewModel.setNetWorkAvailable(it)
        }
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner) {
            if (!it) {
                findNavController().navigate(R.id.no_internet_dest)
            }
        }
    }

    abstract fun getViewBinding(): VBiding
    abstract fun setAppBar()
    abstract fun setupListeners()
    abstract fun setViews()

    private fun resetValue() {
        viewModel.errorMessage.value = ""
        viewModel.toastMessage.value = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        viewModel.facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        viewModel.callbackManager().onActivityResult(requestCode, resultCode, data, object : LoginListener {
            override fun callback(result: MyResult<Boolean>) {
                Log.d(TAG, "onActivityResult: result=$result")
                if (result is MyResult.Success) {
                    viewModel.logInStatus.value = BaseLoadingStatus.SUCCEEDED
                } else if (result is MyResult.Error) {
                    viewModel.logInStatus.value = BaseLoadingStatus.FAILED
                    viewModel.toastMessage.value = result.exception.message
                }
            }
        }
        )
        super.onActivityResult(requestCode, resultCode, data)
    }

    abstract fun setLoading(loading: Boolean)

    protected fun validRestore(restoreStatus: BaseLoadingStatus) {
        when (restoreStatus) {
            BaseLoadingStatus.LOADING -> {
                setLoading(true)
            }
            BaseLoadingStatus.SUCCEEDED -> {
                setLoading(false)
                Utils.launchHome(requireContext())
                activity?.finish()
            }
            else -> {
                setLoading(false)
            }
        }
    }

    protected fun loginWithFacebook() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile", "email"))

        viewModel.logInWithFacebook()
    }
}