package com.goldenowl.ecommerce.models.repo

import android.net.Uri
import android.util.Log
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.utils.PasswordUtils

class LocalAuthDataSource(private val userManager: UserManager) : AuthDataSource {
    override fun isLogin(): Boolean {
        return userManager.isLoggedIn()
    }

    override fun logOut() {
        userManager.logOut()
    }

    override fun getUserId(): String {
        Log.d("LocalAuthDataSource", "getUserId: ${userManager.id}")
        return userManager.id
    }

     fun updateUserData(fullName: String, dob: String){
        userManager.apply {
            this.name = fullName
            this.dob = dob
        }
    }

    fun changePassword(oldPw: String, newPw: String) {
        userManager.hash = PasswordUtils.md5(newPw)
    }

    fun updateAvatar(file: Uri?) {
        userManager.avatar = file.toString()
        // todo cache image
    }
}