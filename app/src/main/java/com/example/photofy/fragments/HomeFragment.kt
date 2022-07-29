package com.example.photofy.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.viewpager2.widget.ViewPager2
import com.example.photofy.PhotofyApplication
import com.example.photofy.R
import com.example.photofy.activities.MainActivity.Companion.fabCompose
import com.example.photofy.adapters.PostsAdapter
import com.example.photofy.models.Post
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.parse.*
import com.parse.boltsinternal.Task
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import java.util.*


class HomeFragment : Fragment() {
    private lateinit var viewpagerPosts: ViewPager2
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var adapter: PostsAdapter
    private lateinit var allPosts: MutableList<Post>
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
        val validUsers: List<String> = ParseUser.getCurrentUser().getList("Following")!!

        val validUsersBackup: MutableList<String> = ArrayList(validUsers)
        validUsersBackup.add(ParseUser.getCurrentUser().objectId)
        query.whereContainedIn(Post.KEY_USER, validUsersBackup)
        query.addDescendingOrder(Post.KEY_CREATED)
        query.fromLocalDatastore().findInBackground().continueWithTask(
            { task: Task<List<Post>?> ->
                // Update UI with results from Local Datastore
                val error = task.error
                if (error == null) {
                    val posts = task.result!!

                    // Save received posts to list and notify adapter of new data
                    allPosts.addAll(posts)
                    adapter.notifyItemRangeInserted(0, posts.size)
                }

                // Update cache with new query
                query.fromNetwork().findInBackground()
            }, ContextCompat.getMainExecutor(requireContext())
        ).continueWithTask(
            { task: Task<List<Post>> ->
                val error = task.error
                if (error == null) {
                    val posts = task.result!!
                    allPosts.addAll(posts)
                    adapter.notifyItemRangeInserted(0, posts.size)

                    if (adapter.itemCount == 0) {
                        createGuideview()
                    }

                    // Release any objects previously pinned for this query
                    ParseObject.unpinAllInBackground(allPosts,
                        DeleteCallback { e ->
                            if (e != null) {
                                return@DeleteCallback
                            }
                            // Add the latest results for this query to the cache
                            ParseObject.pinAllInBackground(allPosts)
                        })
                }
                task
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun createGuideview() {
        GuideView.Builder(context)
            .setTitle("Create your first post!")
            .setContentText("Take an image and let \n us generate you a song!")
            .setDismissType(DismissType.anywhere)
            .setTargetView(fabCompose)
            .setContentTextSize(12) //optional
            .setTitleTextSize(14) //optional
            .build()
            .show()
    }

    companion object {
        const val TAG = "HomeFragment"
        private val CLIENT_ID = PhotofyApplication.spotifyKey
        private const val REDIRECT_URI = "intent://"
        @JvmField
        var mSpotifyAppRemote: SpotifyAppRemote? = null
    }
}