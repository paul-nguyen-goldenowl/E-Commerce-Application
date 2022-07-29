package com.goldenowl.ecommerce.models.repo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.goldenowl.ecommerce.NotLoggedInException
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.auth.UserManager.Companion.TYPEFACEBOOK
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.models.data.User
import com.goldenowl.ecommerce.utils.MyResult
import com.goldenowl.ecommerce.utils.PasswordUtils.md5
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.CoroutineContext


class RemoteAuthDataSource(private val userManager: UserManager, val context: Context) : AuthDataSource {
    private val dispatchers: CoroutineContext = Dispatchers.IO
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser = firebaseAuth.currentUser

    private val userRef = db.collection("users")

    private val storageRef = Firebase.storage.reference
    private val settingsManager = SettingsManager(context)

    val defaultAvatarRef = storageRef.child("images/avatar/default")

    override fun isLogin(): Boolean {
        return currentUser != null
    }

    override fun logOut() {
        when (userManager.logType) {
            UserManager.TYPEFACEBOOK -> {
                logOutFacebook()
            }
            else -> {
                if (currentUser != null)
                    firebaseAuth.signOut()
                else {
                    Log.d(AuthRepository.TAG, "logOut: current user: ${firebaseAuth.currentUser}")
                }

            }
        }
    }

    override fun getUserId(): String? {
        return currentUser?.uid
    }

    suspend fun updateUserData(updatedUser: User): String? {
        return withContext(Dispatchers.IO) {
            try {
                val currentUserRef = userRef.document(getUserId()!!)
                val userDataSnapshot = currentUserRef.get(Source.SERVER).await()
                if (userDataSnapshot.exists()) {
                    val user = userDataSnapshot.toObject(User::class.java)
                    if (user != null) {
                        user.name = updatedUser.name
                        user.dob = updatedUser.dob
                        user.avatar = updatedUser.avatar
                        user.settings = updatedUser.settings
                        currentUserRef.set(user)
                    }
                }
                return@withContext null
            } catch (e: Exception) {
                return@withContext e.message
            }
        }
    }

    val facebookCallbackManager = CallbackManager.Factory.create() //facebook callback

    val mGoogleSignInClient: GoogleSignInClient

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.web_client_id))
        .requestEmail()
        .build()

    val googleCallbackManager = object : ICallback {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, listener: LoginListener) {
            if (requestCode != GOOGLE_SIGN_IN) {
                return
            }
            if (data == null)
                return
            try {

                val googleAccount = GoogleSignIn.getSignedInAccountFromIntent(data).result
                googleAccount?.let {
                    if (it.email == null || it.idToken == null) {
                        listener.callback(MyResult.Error(NotLoggedInException("User not found")))
                    } else {
                        val firebaseCredential = GoogleAuthProvider.getCredential(it.idToken, null)
                        firebaseAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                currentUser = firebaseAuth.currentUser
                                listener.callback(MyResult.Success(true))
                                onLoginSuccess(currentUser, UserManager.TYPEGOOGLE)

                            }
                            .addOnFailureListener { e ->
                                listener.callback(MyResult.Error(e))
                            }
                    }
                }
            } catch (e: Exception) {
                listener.callback(MyResult.Error(e))
                Log.e(TAG, "loginwithGoogle: ", e)
            }
        }
    }

    init {
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }


    suspend fun signUpWithEmail(email: String, password: String, name: String): String? {
        return withContext(dispatchers) {

            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                currentUser = firebaseAuth.currentUser
                val avatar: String = if (currentUser?.photoUrl != null)
                    currentUser?.photoUrl.toString()
                else
                    defaultAvatarRef.downloadUrl.await().toString()

                if (currentUser != null) {
                    val user = User(
                        id = currentUser!!.uid,
                        name = name,
                        email = currentUser!!.email!!,
                        dob = "",
                        password = md5(password),
                        avatar = avatar,
                        logType = UserManager.TYPEEMAIL
                    )
                    onSignUpSuccess(user)
                }
            } catch (e: Exception) {
                return@withContext e.message
            }
            return@withContext null
        }
    }

    fun logOutFacebook() {
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()

                }).executeAsync()
        }
    }


    fun logInWithFacebook(listener: LoginListener) {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken, listener)
                    listener.callback(MyResult.Success(true))
                }

                override fun onCancel() {
                    Log.d("MainActivity", "Facebook onCancel.")

                }

                override fun onError(error: FacebookException) {
                    Log.d("MainActivity", "Facebook onError.")
                    listener.callback(MyResult.Error(error))
                }
            })
    }


    private fun handleFacebookAccessToken(token: AccessToken, listener: LoginListener) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser = firebaseAuth.currentUser
                    listener.callback(MyResult.Success(true))
                    onLoginSuccess(currentUser, TYPEFACEBOOK)

                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    listener.callback(MyResult.Error(java.lang.Exception(task.exception)))
                }
            }
    }

    private fun onLoginSuccess(currentUser: FirebaseUser?, logType: String) {
//        check  if user exist,
        val userId = currentUser!!.uid
        val cUserRef = userRef.document(userId)
        cUserRef.get(Source.SERVER).addOnCompleteListener {
            val doc = it.result
            if (!doc.exists()) {
                val user = User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "",
                    email = currentUser.email ?: currentUser.phoneNumber ?: userId,
                    dob = "",
                    password = "",
                    avatar = currentUser?.photoUrl.let { it.toString() },
                    logType = logType
                )
                cUserRef.set(user).addOnCompleteListener {
                    Log.d(TAG, "onLoginSuccess: add new user successfully")
                }
                userManager.addAccount(user)
            }
        }
            .addOnFailureListener {
                Log.w(TAG, "addUserToFireStore: ERROR", it)
            }
    }

    private fun onSignUpEmailSuccess(currentUser: FirebaseUser?, logType: String, name: String) {
        if (currentUser == null)
            Log.d(TAG, "onLoginSuccess: firebaseAuth.currentUser NULL ")
        else {
            val user = User(
                id = currentUser.uid,
                name = name,
                email = currentUser.email ?: "",
                dob = "",
                password = "",
                avatar = currentUser?.photoUrl.let { it.toString() },
                logType = logType
            )
            addNewUserToFireStore(user)
        }
    }

    suspend fun restoreUserData(userId: String) {
        val userDataSnapshot = userRef.document(userId).get().await()
        val user = userDataSnapshot.toObject(User::class.java)
        userManager.addAccount(user!!) // restore local
        /* restore settings to Preference */
        if (user.settings.isNotEmpty())
            settingsManager.saveUserSettings(user.settings)
    }

    private fun addNewUserToFireStore(user: User) {
        val cUserRef = user.id.let { userRef.document(it) }
        cUserRef?.get()?.addOnCompleteListener {
            val doc = it.result
            if (!doc.exists()) {
                cUserRef.set(user).addOnCompleteListener {
                    Log.d(TAG, "addNewUserToFireStore: add new user successfully")
                }
                userManager.addAccount(user)
            }
        }
    }

    suspend fun logInWithEmail(email: String, password: String): String? {
        return withContext(dispatchers) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val currentUser = firebaseAuth.currentUser
                onLoginSuccess(currentUser, UserManager.TYPEEMAIL)
            } catch (e: Exception) {
                Log.e(TAG, "logInWithEmail: ERROR", e)
                return@withContext e.message
            }
            return@withContext null
        }

    }

    private fun onSignUpSuccess(user: User) {
        if (currentUser == null)
            return // todo return result
        else {
            userManager.addAccount(user)
            addNewUserToFireStore(user)
        }
    }

    suspend fun forgotPassword(email: String): String? {

        return withContext(dispatchers) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "forgotPassword: error:", e)
                val msg = e?.message
                return@withContext msg
            }
            return@withContext null
        }
    }

    fun logInWithGoogle(fragment: Fragment) {
        val signInIntent = mGoogleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): String? {
        return withContext(dispatchers) {
            try {
                val credential = EmailAuthProvider
                    .getCredential(userManager.email, oldPassword)
                currentUser?.reauthenticate(credential)?.await()
                currentUser?.updatePassword(newPassword)?.await()
                userManager.hash = md5(newPassword)
                updateFirestore()

                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "changePassword: ERROR", e)
                return@withContext e.message
            }
        }
    }

    private fun updateFirestore() {
        val user = userManager.getUser()
        user.id.let {
            userRef.document(it).set(user).addOnCompleteListener {
                Log.d(TAG, "updateFirestore: successful")
            }
                .addOnFailureListener {
                    Log.d(TAG, "updateFirestore: ERROR", it)
                }
        }
    }

    suspend fun uploadAvatar(userId: String, file: File): String {
        val stream = FileInputStream(file)
        val avatarRef = storageRef.child("images/avatar/$userId")
        avatarRef.putStream(stream).await()
        val url = avatarRef.downloadUrl.await()
        return url.toString()
    }

    suspend fun updateAvatar(userId: String, url: String) {
        userRef.document(userId).update("avatar", url).await()
    }

    suspend fun getUserById(userId: String): User {
        val user = userRef.document(userId).get(Source.SERVER).await()
        return user.toObject(User::class.java) ?: throw Exception("User $userId not found")
    }


    companion object {
        const val TAG = "RemoteAuthDataSource"
        const val GOOGLE_SIGN_IN = 100
    }
}

interface LoginListener {
    fun callback(result: MyResult<Boolean>)
}

interface ICallback {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, listener: LoginListener)
}
