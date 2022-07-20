package com.example.photofy.activities

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.photofy.R
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.example.photofy.fragments.CommentsFragment
import com.google.android.material.navigation.NavigationBarView
import com.example.photofy.fragments.HomeFragment
import com.example.photofy.fragments.SearchFragment
import com.example.photofy.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spotifyToken = intent.getStringExtra("token")

        val sharedPreferences = getSharedPreferences("SPOTIFY", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", spotifyToken)
        editor.apply()

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
}