package com.goldenowl.ecommerce.models.repo

import android.net.Uri
import androidx.fragment.app.Fragment
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.MyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val localAuthDataSource: LocalAuthDataSource
) {

    fun isUserLoggedIn(): Boolean {
        return localAuthDataSource.isLogin()
    }


    fun getUserId(): String {
        return localAuthDataSource.getUserId()
    }

    fun logOut() {
        remoteAuthDataSource.logOut()
        localAuthDataSource.logOut()
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): String? {
        return remoteAuthDataSource.signUpWithEmail(email, password, name)
    }

    fun logInWithFacebook(fragment: Fragment, listener: LoginListener) {
        remoteAuthDataSource.logInWithFacebook(fragment, listener)
    }

    suspend fun logInWithEmail(email: String, password: String): String? {
        return remoteAuthDataSource.logInWithEmail(email, password)
    }

    suspend fun forgotPassword(email: String): String? {
        return remoteAuthDataSource.forgotPassword(email)
    }

    fun logInWithGoogle(fragment: Fragment) {
        remoteAuthDataSource.logInWithGoogle(fragment)
    }

    suspend fun changePassword(oldPw: String, newPw: String): String? {
        val res = remoteAuthDataSource.changePassword(oldPw, newPw)
        if (res.isNullOrEmpty())
            localAuthDataSource.changePassword(oldPw, newPw)
        return res
    }

    fun callBackManager(): ICallback {
        return remoteAuthDataSource.googleCallbackManager
    }

    suspend fun updateAvatar(userId: String, file: Uri?): String? {
        val err = remoteAuthDataSource.updateAvatar(userId, file)
        if (err.isNullOrEmpty()) localAuthDataSource.updateAvatar(file)
        return err
    }

    suspend fun updateUserData(user: User): String? {
        localAuthDataSource.updateUserData(user)
        return remoteAuthDataSource.updateUserData(user)
    }

    suspend fun getUserById(userId: String): MyResult<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = remoteAuthDataSource.getUserById(userId)
                return@withContext MyResult.Success(user)
            } catch (e: Exception) {
                return@withContext MyResult.Error(e)
            }
        }
    }

    fun getUser(): User? {
        return localAuthDataSource.getUser()
    }

    companion object {
        val TAG = "AuthRepository"
    }
}
