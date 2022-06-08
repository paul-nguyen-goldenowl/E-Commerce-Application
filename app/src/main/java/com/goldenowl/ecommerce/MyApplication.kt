package com.goldenowl.ecommerce

import android.app.Application
import android.content.Context
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.models.data.ECommerceDatabase
import com.goldenowl.ecommerce.models.data.LocalProductsDataSource
import com.goldenowl.ecommerce.models.data.RemoteProductsDataSource
import com.goldenowl.ecommerce.models.repo.AuthRepository
import com.goldenowl.ecommerce.models.repo.LocalAuthDataSource
import com.goldenowl.ecommerce.models.repo.ProductsRepository
import com.goldenowl.ecommerce.models.repo.RemoteAuthDataSource
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    private val TAG = "MyApplication"
    private val userManager by lazy {UserManager.getInstance(this)}
    private val database by lazy { ECommerceDatabase.getDatabase(this) }

    lateinit var productsRepository: ProductsRepository
    lateinit var authRepository: AuthRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: app on create")
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        FirebaseApp.initializeApp(this)

        authRepository = AuthRepository(RemoteAuthDataSource(userManager, this), LocalAuthDataSource(userManager))
        productsRepository = ProductsRepository(RemoteProductsDataSource(), createLocalProductsDataSource(this))
    }



    private fun createLocalProductsDataSource(context: Context): LocalProductsDataSource {
        Log.d(TAG, "createLocalProductsDataSource: create database")

        val productDao = database.productDao()
        val userOrderDao = database.userOrderDao()

        return LocalProductsDataSource(productDao, userOrderDao)
    }
}