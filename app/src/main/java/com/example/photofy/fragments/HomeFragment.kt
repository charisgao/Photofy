package com.example.photofy.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.viewpager2.widget.ViewPager2
import com.example.photofy.BitmapScaler
import com.example.photofy.PhotofyApplication
import com.example.photofy.R
import com.example.photofy.activities.CameraActivity
import com.example.photofy.activities.ImageResultsActivity
import com.example.photofy.activities.MainActivity
import com.example.photofy.adapters.PostsAdapter
import com.example.photofy.models.Photo
import com.example.photofy.models.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.parse.*
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment() : Fragment() {
    private lateinit var viewpagerPosts: ViewPager2
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var fabCompose: FloatingActionButton
    private lateinit var adapter: PostsAdapter
    private lateinit var allPosts: MutableList<Post>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var connectionParams: ConnectionParams
    private lateinit var connectionListener: ConnectionListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewpagerPosts = view.findViewById(R.id.viewpagerPosts)
        swipeContainer = view.findViewById(R.id.swipeContainer)
        fabCompose = view.findViewById(R.id.fabCompose)
        allPosts = ArrayList()
        adapter = PostsAdapter(
            context,
            allPosts,
            findNavController(requireActivity(), R.id.navHostFragment)
        )

        // Set the adapter on the ViewPager2
        viewpagerPosts.adapter = adapter
        connectSpotifyAppRemote()
        queryPosts()
        swipeContainer.setOnRefreshListener(OnRefreshListener {
            adapter.clear()
            queryPosts()
            swipeContainer.isRefreshing = false
        })
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        fabCompose.setOnClickListener(View.OnClickListener { v -> showPopup(v) })
        galleryLauncher = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { selectedImage ->
            if (selectedImage != null) {
                try {
                    val source = ImageDecoder.createSource(
                        requireContext().contentResolver, selectedImage
                    )
                    val bitmap = ImageDecoder.decodeBitmap(source)

                    // Scale the image smaller
                    val resizedBitmap = BitmapScaler.scaleToFitHeight(bitmap, 200)

                    // Store smaller bitmap to disk
                    // Configure byte output stream
                    val bytes = ByteArrayOutputStream()
                    // Compress the image further
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes)
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
                        val i = Intent(context, ImageResultsActivity::class.java)
                        i.putExtra("filePath", resizedFile.absolutePath)
                        i.putExtra("photo", picture)
                        i.putExtra("gallery", true)
                        startActivity(i)
                        Log.i(TAG, "Photo saved successfully")
                    }
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "File not found error through gallery $e")
                } catch (e: IOException) {
                    Log.e(TAG, "IOException with gallery $e")
                }
            }
        }
    }

    private fun showPopup(v: View?) {
        val popup = PopupMenu(context, v, Gravity.RIGHT)
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
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun enableCamera() {
        val i = Intent(context, CameraActivity::class.java)
        startActivity(i)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            (context as MainActivity?)!!,
            REQUIRED_PERMISSIONS,
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun launchGallery() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        if (i.resolveActivity(requireContext().packageManager) != null) {
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
                File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory")
            }

            // Return the file target for the photo based on timestamp
            return File(mediaStorageDir.path + File.separator + dateFormat.format(Date()) + ".jpg")
        }

    private fun connectSpotifyAppRemote() {
        connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
        connectionListener = object : ConnectionListener {
            override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote
            }

            override fun onFailure(error: Throwable) {
                Log.e(TAG, "SpotifyAppRemote connection error")
            }
        }
        SpotifyAppRemote.connect(context, connectionParams, connectionListener)
    }

    override fun onStart() {
        super.onStart()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
        SpotifyAppRemote.connect(context, connectionParams, connectionListener)
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    override fun onResume() {
        super.onResume()
        connectSpotifyAppRemote()
    }

    override fun onPause() {
        super.onPause()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    private fun queryPosts() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        query.limit = 20
        val validUsers: MutableList<String> = ParseUser.getCurrentUser().getList<String>("Following")!!
        validUsers.add(ParseUser.getCurrentUser().objectId)
        query.whereContainedIn(Post.KEY_USER, validUsers)
        query.addDescendingOrder(Post.KEY_CREATED)
        query.findInBackground(object : FindCallback<Post> {
            override fun done(posts: List<Post>, e: ParseException?) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e)
                    return
                }
                validUsers.remove(ParseUser.getCurrentUser().objectId)
                // Save received posts to list and notify adapter of new data
                allPosts.addAll(posts)
                adapter.notifyDataSetChanged()
            }
        })
    }

    companion object {
        const val TAG = "HomeFragment"
        private val CLIENT_ID = PhotofyApplication.spotifyKey
        private const val REDIRECT_URI = "intent://"
        @JvmField
        var mSpotifyAppRemote: SpotifyAppRemote? = null
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val PERMISSIONS_REQUEST_CODE = 1001
    }
}