package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.facebook.CallbackManager
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.models.repo.ICallback
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as MyApplication).authRepository
    private val productsRepository = (application as MyApplication).productsRepository
    private val settingManager: SettingsManager = SettingsManager(application as MyApplication)

    fun logOut() {
        authRepository.logOut()
    }

    val toastMessage = MutableLiveData<String?>()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    val facebookCallbackManager = CallbackManager.Factory.create() //facebook callback

    var signUpStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var logInStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var changePasswordStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var forgotPasswordStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()

    var errorMessage: MutableLiveData<String?> = MutableLiveData<String?>()

    init {
        signUpStatus.value = BaseLoadingStatus.NONE
        logInStatus.value = BaseLoadingStatus.NONE
        changePasswordStatus.value = BaseLoadingStatus.NONE
        forgotPasswordStatus.value = BaseLoadingStatus.NONE
    }


    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            signUpStatus.value = BaseLoadingStatus.LOADING
            val result = authRepository.signUpWithEmail(email, password, name)
            onDone(result, signUpStatus)
        }
    }

    fun logInWithFacebook(fragment: Fragment) {
        authRepository.logInWithFacebook(fragment)
    }

    fun logInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            logInStatus.value = BaseLoadingStatus.LOADING
            val res = authRepository.logInWithEmail(email, password)
            onDone(res, logInStatus)
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            forgotPasswordStatus.value = BaseLoadingStatus.LOADING
            val res = authRepository.forgotPassword(email)
            if (res.isNullOrBlank())
                toastMessage.value = getApplication<MyApplication>().getString(R.string.email_rs_password_sent, email)
            onDone(res, forgotPasswordStatus)
        }
    }

    private fun onDone(res: String?, status: MutableLiveData<BaseLoadingStatus>) {
        errorMessage.value = res
        if (res.isNullOrBlank())
            status.value = BaseLoadingStatus.SUCCEEDED
        else
            status.value = BaseLoadingStatus.FAILED
    }

    fun logInWithGoogle(fragment: Fragment) {
        authRepository.logInWithGoogle(fragment)
    }

    fun emptyErrorMessage() {
        errorMessage.value = ""
    }

    fun changePassword(oldPw: String, newPw: String) {
        viewModelScope.launch {
            changePasswordStatus.value = BaseLoadingStatus.LOADING
            val res = authRepository.changePassword(oldPw, newPw)
            onDone(res, changePasswordStatus)
        }
    }

    fun callbackManager(): ICallback {
        return authRepository.callBackManager()
    }

    fun updateAvatar(file: Uri?) {
        viewModelScope.launch {
            val res = authRepository.updateAvatar(authRepository.getUserId(), file)
        }
    }

    fun saveUserSettings(fullName: String, dob: String, settings: Map<String, Boolean>) {
        settingManager.saveUserSettings(settings)
        viewModelScope.launch {
            val err = authRepository.updateUserData(fullName, dob, settings)
            toastMessage.value = if (err.isNullOrEmpty()) "Apply changes successfully" else err
        }
    }

    companion object {
        const val TAG = "AuthViewModel"
    }
}
