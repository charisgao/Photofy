package com.example.photofy.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.photofy.PushNotificationService
import com.example.photofy.R
import com.example.photofy.fragments.HomeFragment
import com.example.photofy.fragments.ProfileFragment
import com.example.photofy.fragments.SearchFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.messaging.FirebaseMessaging
import com.parse.ParseUser

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private val fragmentManager = supportFragmentManager
    private val KEY_DEVICE_TOKEN = "DeviceToken"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spotifyToken = intent.getStringExtra("token")

        val sharedPreferences = getSharedPreferences("SPOTIFY", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", spotifyToken)
        editor.apply()

        saveToken()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment?
        navController = navHostFragment!!.navController

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.itemIconTintList = null

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.commentsFragment -> bottomNavigationView.visibility = View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }

        bottomNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            lateinit var fragment: Fragment
            when (item.itemId) {
                R.id.action_home -> fragment = HomeFragment()
                R.id.action_search -> fragment = SearchFragment()
                R.id.action_profile -> fragment = ProfileFragment()
                else -> {}
            }
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit()
            true
        })
        // Set default selection
        bottomNavigationView.selectedItemId = R.id.action_home
    }

    private fun saveToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(PushNotificationService.TAG, "Fetching device token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            val current = ParseUser.getCurrentUser()
            if (!token.equals(current.getString(KEY_DEVICE_TOKEN))) {
                current.put(KEY_DEVICE_TOKEN, token)
                current.saveInBackground()
            }
        })
    }
}