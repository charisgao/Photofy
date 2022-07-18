package com.example.photofy.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photofy.R
import com.example.photofy.activities.EditProfileActivity
import com.example.photofy.activities.LoginActivity
import com.example.photofy.activities.MainActivity
import com.example.photofy.activities.SettingsActivity
import com.example.photofy.adapters.PostsAdapter
import com.example.photofy.adapters.ProfileAdapter
import com.example.photofy.models.Follow
import com.example.photofy.models.Post
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.parse.FindCallback
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser

class ProfileFragment : Fragment {
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profilePosts: MutableList<Post>
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var tbProfile: MaterialToolbar
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileBiography: TextView
    private lateinit var tvNumberPosts: TextView
    private lateinit var tvNumberLikes: TextView
    private lateinit var tvNumberFollowers: TextView
    private lateinit var tvNumberFollowing: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnFollow: Button
    private lateinit var btnFollowing: Button
    private lateinit var rvProfilePosts: RecyclerView

    private var user = ParseUser.getCurrentUser()
    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000"))
    private lateinit var deleteIcon: Drawable

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
        tvNumberPosts = view.findViewById(R.id.tvNumberPosts)
        tvNumberLikes = view.findViewById(R.id.tvNumberLikes)
        tvNumberFollowers = view.findViewById(R.id.tvNumberFollowers)
        tvNumberFollowing = view.findViewById(R.id.tvNumberFollowing)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnFollow = view.findViewById(R.id.btnFollow)
        btnFollowing = view.findViewById(R.id.btnFollowing)
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
        setFollowerCount()
        setFollowingCount()

        if (user.objectId == ParseUser.getCurrentUser().objectId) {
            btnEditProfile.visibility = View.VISIBLE
            btnFollow.visibility = View.GONE
            btnFollowing.visibility = View.GONE
        } else {
            btnEditProfile.visibility = View.GONE
            if (ParseUser.getCurrentUser().getList<String>("Following")!!.contains(user.objectId)) {
                btnFollow.visibility = View.GONE
                btnFollowing.visibility = View.VISIBLE
            } else {
                btnFollow.visibility = View.VISIBLE
                btnFollowing.visibility = View.GONE
            }
        }

        btnFollow.setOnClickListener(View.OnClickListener { followUser() })
        btnFollowing.setOnClickListener(View.OnClickListener { unfollowUser() })

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

        deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                val recentlyDeletedItem = profilePosts[position]
                var undo = false

                profilePosts.removeAt(position)
                profileAdapter.notifyItemRemoved(position)
                val snackbar: Snackbar = Snackbar.make(
                    view, "Deleted post",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction("Undo") {
                    profilePosts.add(
                        position,
                        recentlyDeletedItem
                    )
                    profileAdapter.notifyItemInserted(position)
                    undo = true
                }

                snackbar.addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onShown(transientBottomBar: Snackbar?) {
                        super.onShown(transientBottomBar)
                    }

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if (!undo) {
                            deletePost(recentlyDeletedItem)
                        }
                    }
                })
                snackbar.show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView

                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                // swipe right
                if (dX > 0 ) {
                    swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                        itemView.bottom - iconMargin)
                } else { // swipe left
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                        itemView.top + iconMargin,
                        itemView.right - iconMargin,
                        itemView.bottom - iconMargin)
                }

                swipeBackground.draw(c)

                c.save()

                if (dX > 0) {
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                } else {
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }
                deleteIcon.draw(c)

                c.restore()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rvProfilePosts)

        queryPosts()
    }

    private fun deletePost(post: Post) {
        val query = ParseQuery.getQuery(Post::class.java)
        query.whereEqualTo(Post.KEY_OBJECT_ID, post.objectId)
        query.findInBackground(FindCallback { objects, e -> // Check for errors
            if (e != null) {
                Log.e(PostsAdapter.TAG, "Issue with finding like to delete $e")
                return@FindCallback
            }
            if (objects.isNotEmpty()) {
                objects[0].deleteInBackground()
            }
        })
    }

    private fun setPostCount() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.whereEqualTo(Post.KEY_USER, user)
        query.countInBackground { count, _ -> tvNumberPosts.text = count.toString() }
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

    private fun setFollowerCount() {
        val query = ParseQuery.getQuery(Follow::class.java)
        query.whereEqualTo(Follow.KEY_TO, user)
        query.countInBackground { count, _ -> tvNumberFollowers.text = count.toString() }
    }

    private fun setFollowingCount() {
        tvNumberFollowing.text = user.getList<String>("Following")!!.size.toString()
    }

    private fun followUser() {
        val follow = Follow()
        follow.from = ParseUser.getCurrentUser()
        follow.to = user
        follow.saveInBackground()

        val following = ParseUser.getCurrentUser().getList<String>("Following")!!
        following.add(user.objectId)
        ParseUser.getCurrentUser().put("Following", following)
        ParseUser.getCurrentUser().saveInBackground()

        btnFollow.visibility = View.GONE
        btnFollowing.visibility = View.VISIBLE
    }

    private fun unfollowUser() {
        val query = ParseQuery.getQuery(Follow::class.java)
        query.whereEqualTo(Follow.KEY_FROM, ParseUser.getCurrentUser())
        query.whereEqualTo(Follow.KEY_TO, user)
        val followList: MutableList<Follow> = query.find()
        val userToUnfollow = followList[0].to

        val following = ParseUser.getCurrentUser().getList<String>("Following")!!
        following.remove(userToUnfollow.objectId)
        ParseUser.getCurrentUser().put("Following", following)
        ParseUser.getCurrentUser().saveInBackground()

        followList[0].deleteInBackground()

        btnFollow.visibility = View.VISIBLE
        btnFollowing.visibility = View.GONE
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
    }

    private fun goLoginActivity() {
        val i = Intent(context as MainActivity?, LoginActivity::class.java)
        startActivity(i)
        (context as MainActivity?)!!.finish()
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

    companion object {
        const val TAG = "ProfileFragment"
        private const val REQUEST_CODE = 1337
    }
}