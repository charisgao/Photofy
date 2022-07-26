package com.example.photofy.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.photofy.*
import com.example.photofy.fragments.HomeFragment
import com.example.photofy.fragments.ProfileFragment
import com.example.photofy.fragments.SearchFragment
import com.example.photofy.models.Photo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.messaging.FirebaseMessaging
import com.parse.ParseFile
import com.parse.ParseUser
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), ProgressActivityListener {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var progressBar: ProgressBar
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spotifyToken = intent.getStringExtra("token")

        val sharedPreferences = getSharedPreferences("SPOTIFY", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", spotifyToken)
        editor.apply()

        saveFirebaseToken()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment?
        navController = navHostFragment!!.navController

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.itemIconTintList = null

        progressBar = findViewById(R.id.progressBar)

        fabCompose = findViewById(R.id.fabCompose)
        fabCompose.setOnClickListener(View.OnClickListener { v -> showPopup(v) })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.commentsFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    fabCompose.visibility = View.GONE }
                else -> { bottomNavigationView.visibility = View.VISIBLE
                    fabCompose.visibility = View.VISIBLE }
            }
        }

        bottomNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            lateinit var fragment: Fragment
            when (item.itemId) {
                R.id.action_home -> fragment = HomeFragment()
                R.id.action_search -> {
                    showProgressBar()
                    fragment = SearchFragment()
                }
                R.id.action_profile -> fragment = ProfileFragment()
                else -> {}
            }
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit()
            true
        })
        // Set default selection
        bottomNavigationView.selectedItemId = R.id.action_home

        // TODO: improve upload image quality
        galleryLauncher = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { selectedImage ->
            if (selectedImage != null) {
                try {
                    val source = ImageDecoder.createSource(this.contentResolver, selectedImage)
                    val bitmap = ImageDecoder.decodeBitmap(source)

                    // Scale the image smaller
                    val resizedBitmap = BitmapScaler.scaleToFitHeight(bitmap, 200)

                    // Store smaller bitmap to disk
                    // Configure byte output stream
                    val bytes = ByteArrayOutputStream()
                    // Compress the image further
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
                    // Create a new file for the resized bitmap
                    val resizedFile = photoFile
                    try {
                        resizedFile.createNewFile()
                        var fos: FileOutputStream? = null
                        try {
                            fos = FileOutputStream(resizedFile)
                            // Write the bytes of the bitmap to file
                            fos.write(bytes.toByteArray())
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } finally {
                            fos!!.close()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val picture = Photo()
                    picture.user = ParseUser.getCurrentUser()
                    picture.image = ParseFile(resizedFile)
                    picture.saveInBackground {
                        val i = Intent(this, ImageResultsActivity::class.java)
                        i.putExtra("filePath", photoFile.absolutePath)
                        i.putExtra("photo", picture)
                        i.putExtra("gallery", true)
                        startActivity(i)
                        Log.i(HomeFragment.TAG, "Photo saved successfully")
                    }
                } catch (e: FileNotFoundException) {
                    Log.e(HomeFragment.TAG, "File not found error through gallery $e")
                } catch (e: IOException) {
                    Log.e(HomeFragment.TAG, "IOException with gallery $e")
                }
            }
        }
    }

    private fun saveFirebaseToken() {
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

    private fun showPopup(v: View?) {
        val popup = PopupMenu(this, v, Gravity.RIGHT)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.miOpenCamera -> {
                    if (allPermissionsGranted()) {
                        enableCamera()
                    } else {
                        requestPermission()
                    }
                    true
                }
                R.id.miUploadImage -> {
                    launchGallery()
                    true
                }
                else -> false
            }
        })
        popup.inflate(R.menu.menu_compose)
        popup.show()
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission: String in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun enableCamera() {
        val i = Intent(this, CameraActivity::class.java)
        startActivity(i)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun launchGallery() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        if (i.resolveActivity(this.packageManager) != null) {
            galleryLauncher.launch("image/*")
        }
    }

    // Get safe storage directory for photos
    // getExternalFilesDir used to access package specific directories, so don't need to request external read/write runtime permissions
    private val photoFile: File
        get() {
            // Return the file target for the photo based on timestamp
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)

            // Get safe storage directory for photos
            // getExternalFilesDir used to access package specific directories, so don't need to request external read/write runtime permissions
            val mediaStorageDir =
                File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeFragment.TAG)

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(HomeFragment.TAG, "failed to create directory")
            }

            // Return the file target for the photo based on timestamp
            return File(mediaStorageDir.path + File.separator + dateFormat.format(Date()) + ".jpg")
        }

    override fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    companion object {
        lateinit var fabCompose: FloatingActionButton
        private const val KEY_DEVICE_TOKEN = "DeviceToken"
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val PERMISSIONS_REQUEST_CODE = 1001
    }
}