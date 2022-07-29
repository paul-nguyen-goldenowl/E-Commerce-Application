package com.goldenowl.ecommerce.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.goldenowl.ecommerce.MyApplication
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.models.repo.ICallback
import com.goldenowl.ecommerce.models.repo.LoginListener
import com.goldenowl.ecommerce.utils.BaseLoadingStatus
import com.goldenowl.ecommerce.utils.ImageHelper
import com.goldenowl.ecommerce.utils.MyResult
import com.goldenowl.ecommerce.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = (application as MyApplication).authRepository
    private val productsRepository = (application as MyApplication).productsRepository
    private val settingManager: SettingsManager = SettingsManager(application as MyApplication)

    fun logOut() {
        authRepository.logOut()
        settingManager.clear()
        viewModelScope.launch {
            val clearRes = productsRepository.clearLocalDataSource()
            Log.d(TAG, "logOut: clearRes=$clearRes")
            if (clearRes is MyResult.Error) {
                toastMessage.value = clearRes.exception.message
            }
        }
    }

    val toastMessage = MutableLiveData<String?>()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    val facebookCallbackManager = authRepository.facebookCallbackManager

    var signUpStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var logInStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var restoreStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var changePasswordStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var forgotPasswordStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var uploadAvatarStatus: MutableLiveData<BaseLoadingStatus> = MutableLiveData<BaseLoadingStatus>()
    var errorMessage: MutableLiveData<String?> = MutableLiveData<String?>()
    var isNetworkAvailable =
        MutableLiveData<Boolean>().apply { value = Utils.isNetworkAvailable(application.applicationContext) }

    init {
        signUpStatus.value = BaseLoadingStatus.NONE
        logInStatus.value = BaseLoadingStatus.NONE
        changePasswordStatus.value = BaseLoadingStatus.NONE
        forgotPasswordStatus.value = BaseLoadingStatus.NONE
        uploadAvatarStatus.value = BaseLoadingStatus.NONE
    }


    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            signUpStatus.value = BaseLoadingStatus.LOADING
            val result = authRepository.signUpWithEmail(email, password, name)
            onDone(result, signUpStatus)
        }
    }

    fun logInWithFacebook() {
        logInStatus.value = BaseLoadingStatus.LOADING
        authRepository.logInWithFacebook(object : LoginListener {
            override fun callback(result: MyResult<Boolean>) {
                if (result is MyResult.Success) {
                    logInStatus.value = BaseLoadingStatus.SUCCEEDED
                } else if (result is MyResult.Error) {
                    logInStatus.value = BaseLoadingStatus.FAILED
                    toastMessage.value = result.exception.message
                }
            }
        })
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
            if (!res.isNullOrBlank()) {
                Toast.makeText((getApplication() as MyApplication).applicationContext, res, Toast.LENGTH_SHORT).show()
            }
            onDone(res, forgotPasswordStatus)
        }
    }

    private fun onDone(res: String?, status: MutableLiveData<BaseLoadingStatus>) {
        Log.d(TAG, "onDone: error = $res")
        errorMessage.value = res
        if (res.isNullOrBlank())
            status.value = BaseLoadingStatus.SUCCEEDED
        else
            status.value = BaseLoadingStatus.FAILED
    }

    fun logInWithGoogle(fragment: Fragment) {
        logInStatus.value = BaseLoadingStatus.LOADING
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

    fun updateAvatar(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(
                getApplication<Application>().applicationContext,
                getApplication<Application>().applicationContext.resources.getString(R.string.image_not_found),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        uploadAvatarStatus.value = BaseLoadingStatus.LOADING
        viewModelScope.launch {
            val file = ImageHelper.compressImageUri(getApplication<Application>().applicationContext, uri)
            val res = authRepository.updateAvatar(authRepository.getUserId(), file)
            Log.d(TAG, "updateAvatar: $res")
            if (res is MyResult.Success) {
                uploadAvatarStatus.value = BaseLoadingStatus.SUCCEEDED
            } else if (res is MyResult.Error) {
                uploadAvatarStatus.value = BaseLoadingStatus.FAILED
                toastMessage.postValue(res.exception.message)
            }
        }
    }

    fun saveUserSettings(user: User) {
        viewModelScope.launch {
            settingManager.saveUserSettings(user.settings)
            val err = authRepository.updateUserData(user)
            toastMessage.value = if (err.isNullOrEmpty()) "Apply changes successfully" else err
        }
    }

    fun restoreUserData() {
        restoreStatus.value = BaseLoadingStatus.LOADING

        viewModelScope.launch {
            if (firebaseAuth.currentUser == null) {
                showToast(R.string.error_occurred)
                return@launch
            }
            Log.d(TAG, "restoreUserData: not call if false")
            firebaseAuth.currentUser?.let {
                val userId = it.uid
                val restoreData = async { productsRepository.restoreUserData(userId) }
                val restoreSettings = async { authRepository.restoreUserSettingsData(userId) }
                val restoreDataRes = restoreData.await()
                val restoreSettingsRes = restoreSettings.await()

                Log.d(TAG, "restoreDataRes: $restoreDataRes")
                Log.d(TAG, "restoreSettingsRes: $restoreSettingsRes")

                if (restoreDataRes is MyResult.Success && restoreSettingsRes is MyResult.Success) {
                    restoreStatus.value = BaseLoadingStatus.SUCCEEDED
                } else if (restoreDataRes is MyResult.Error) {
                    restoreStatus.value = BaseLoadingStatus.FAILED
                    toastMessage.value = restoreDataRes.exception.message
                } else if (restoreSettingsRes is MyResult.Error) {
                    restoreStatus.value = BaseLoadingStatus.FAILED
                    toastMessage.value = restoreSettingsRes.exception.message
                }
            }
        }
    }

    private fun showToast(resId: Int) {
        Toast.makeText(
            getApplication<Application>().applicationContext,
            getApplication<Application>().applicationContext.resources.getString(resId),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setNetWorkAvailable(available: Boolean) {
        isNetworkAvailable.value = available
    }

    companion object {
        const val TAG = "AuthViewModel"
    }
}

