/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goldenowl.ecommerce.ui.global

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val TAG: String = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)


        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        val navController = host.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home_dest, R.id.bag_dest, R.id.shop_dest, R.id.favorites_dest, R.id.profile_dest),
        )

        setupBottomNavMenu(navController)

        navController.addOnDestinationChangedListener { controller, destination, _ ->

            Log.d(
                "NavigationActivity",
                "Navigated to=${destination.label} , from= ${navController.previousBackStackEntry?.destination?.label}"
            )

            when (destination.id) {
                R.id.home_dest, R.id.bag_dest, R.id.shop_dest, R.id.favorites_dest, R.id.profile_dest, R.id.category_dest -> {
                    showNavBar(true)
                }
                else -> showNavBar(false)
            }
        }

        setContentView(binding.root)

    }

    fun showNavBar(b: Boolean) {
        binding.bottomNavView?.visibility = if (b) View.VISIBLE else View.GONE
    }


    private fun setupBottomNavMenu(navController: NavController) {
        binding.bottomNavView?.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { item ->
                NavigationUI.onNavDestinationSelected(item, navController)
                navController.popBackStack(item.itemId, inclusive = false)
                true
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

}

