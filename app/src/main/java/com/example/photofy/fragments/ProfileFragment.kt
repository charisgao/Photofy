package com.example.photofy.fragments

import com.example.photofy.adapters.ProfileAdapter
import com.example.photofy.models.Post
import android.content.SharedPreferences
import com.google.android.material.appbar.MaterialToolbar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.parse.ParseUser
import androidx.activity.result.ActivityResultLauncher
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.photofy.R
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.Spannable
import com.parse.ParseObject
import com.parse.GetCallback
import com.parse.ParseFile
import com.bumptech.glide.Glide
import androidx.activity.result.ActivityResultCallback
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.photofy.fragments.ProfileFragment
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.parse.ParseQuery
import com.parse.CountCallback
import com.parse.FindCallback
import com.parse.LogOutCallback
import com.example.photofy.activities.EditProfileActivity
import com.example.photofy.activities.LoginActivity
import com.example.photofy.activities.SettingsActivity
import com.example.photofy.activities.MainActivity
import com.google.android.material.transition.platform.MaterialFadeThrough
import java.util.ArrayList

class ProfileFragment : Fragment {
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profilePosts: MutableList<Post>
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var tbProfile: MaterialToolbar
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileBiography: TextView
    private lateinit var tvNumPosts: TextView
    private lateinit var tvNumberLikes: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var rvProfilePosts: RecyclerView
    private var user = ParseUser.getCurrentUser()
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    constructor() {
        // Required empty public constructor
    }

    constructor(user: ParseUser) {
        this.user = user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tbProfile = view.findViewById(R.id.tbProfile)
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileBiography = view.findViewById(R.id.tvProfileBiography)
        tvNumPosts = view.findViewById(R.id.tvNumPosts)
        tvNumberLikes = view.findViewById(R.id.tvNumberLikes)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        rvProfilePosts = view.findViewById(R.id.rvProfilePosts)
        tbProfile.inflateMenu(R.menu.menu_profile_toolbar)
        val username = SpannableStringBuilder(user.username)
        username.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            username.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tbProfile.title = username
        tbProfile.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.miLogout -> {
                    logout()
                    true
                }
                R.id.miSettings -> {
                    goToSettings()
                    true
                }
                else -> super@ProfileFragment.onOptionsItemSelected(item)
            }
        })
        user.fetchInBackground<ParseObject> { _, _ ->
            val profilePic = user.getParseFile("Profile")
            Glide.with(requireContext()).load(profilePic!!.url).circleCrop().into(ivProfilePicture)
        }
        tvProfileName.text = user.getString("Name")
        tvProfileBiography.text = user.getString("Biography")
        setPostCount()
        setLikeCount()
        if (user == ParseUser.getCurrentUser()) {
            btnEditProfile.visibility = View.VISIBLE
        } else {
            btnEditProfile.visibility = View.GONE
        }
        editProfileLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val newName = data!!.getStringExtra("Name")
                val newUsername = data.getStringExtra("Username")
                val newBio = data.getStringExtra("Bio")
                Log.i(TAG, "got new info $newUsername $newBio")
                val boldNewUsername = SpannableStringBuilder(newUsername)
                boldNewUsername.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    newUsername!!.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tbProfile.title = boldNewUsername
                tvProfileName.text = newName
                tvProfileBiography.text = newBio
            }
        }
        btnEditProfile.setOnClickListener(View.OnClickListener { goToEditProfile() })
        settingsLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "Your password was updated!", Toast.LENGTH_SHORT).show()
            }
        }
        profilePosts = ArrayList()
        profileAdapter = ProfileAdapter(context, profilePosts)
        rvProfilePosts.adapter = profileAdapter
        rvProfilePosts.layoutManager = LinearLayoutManager(context)
        queryPosts()
    }

    private fun setPostCount() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.whereEqualTo(Post.KEY_USER, user)
        query.countInBackground { count, _ -> tvNumPosts.text = count.toString() }
    }

    private fun setLikeCount() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.whereEqualTo(Post.KEY_USER, user)
        query.findInBackground { objects, _ ->
            var count = 0
            for (post in objects) {
                count += post.numLikes
            }
            tvNumberLikes.text = count.toString()
        }
    }

    private fun logout() {
        ParseUser.logOutInBackground { e ->
            if (e != null) {
                Toast.makeText(context, R.string.logout_error_toast, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Issue with logout", e)
            } else {
                // TODO: figure out how to log user out from Spotify
                editor = requireContext().getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE)
                    .edit()
                editor.remove("token")
                editor.commit()
                goLoginActivity()
                Toast.makeText(context, R.string.logout_toast, Toast.LENGTH_SHORT).show()
            }
        }
        val currentUser = ParseUser.getCurrentUser()
    }

    private fun goToEditProfile() {
        val i = Intent(context, EditProfileActivity::class.java)
        editProfileLauncher.launch(i)
        requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.stationary)
    }

    private fun goToSettings() {
        val i = Intent(context, SettingsActivity::class.java)
        settingsLauncher.launch(i)
    }

    private fun queryPosts() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        query.whereEqualTo(Post.KEY_USER, user)
        query.addDescendingOrder(Post.KEY_CREATED)
        query.findInBackground(FindCallback { posts, e -> // Check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e)
                return@FindCallback
            }

            // Save received posts to list and notify adapter of new data
            profilePosts.addAll(posts)
            profileAdapter.notifyDataSetChanged()
        })
    }

    private fun goLoginActivity() {
        val i = Intent(context as MainActivity?, LoginActivity::class.java)
        startActivity(i)
        (context as MainActivity?)!!.finish()
    }

    companion object {
        const val TAG = "ProfileFragment"
        private const val REQUEST_CODE = 1337
    }
}