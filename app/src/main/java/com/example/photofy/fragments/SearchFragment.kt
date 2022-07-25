package com.example.photofy.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photofy.ProgressActivityListener
import com.example.photofy.R
import com.example.photofy.adapters.SearchAdapter
import com.example.photofy.models.Post
import com.example.photofy.models.Song
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.parse.*
import com.parse.boltsinternal.Task

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var ivFilter: ImageView
    private lateinit var cgGenre: ChipGroup
    private lateinit var tvNoPosts: TextView
    private lateinit var rvSearchedPosts: RecyclerView

    private lateinit var progressActivity: ProgressActivityListener

    private lateinit var adapter: SearchAdapter
    private lateinit var allPosts: MutableList<Post>
    private lateinit var filteredPosts: MutableList<Post>
    private var filtered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressActivity = activity as ProgressActivityListener

        etSearch = view.findViewById(R.id.etSearch)
        ivFilter = view.findViewById(R.id.ivFilter)
        cgGenre = view.findViewById(R.id.cgGenre)
        tvNoPosts = view.findViewById(R.id.tvNoPosts)
        rvSearchedPosts = view.findViewById(R.id.rvSearchedPosts)

        cgGenre.visibility = View.GONE
        tvNoPosts.visibility = View.GONE

        allPosts = ArrayList()
        filteredPosts = ArrayList()

        adapter = SearchAdapter(context, allPosts)
        rvSearchedPosts.adapter = adapter
        rvSearchedPosts.layoutManager = GridLayoutManager(context, 2)
        queryPosts()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val searchResults = search(s.toString())
                adapter.submitList(searchResults)
            }
        })

        var pressed = false
        ivFilter.setOnClickListener {
            pressed = !pressed
            if (pressed) {
                cgGenre.visibility = View.VISIBLE
            } else {
                cgGenre.visibility = View.GONE
            }
        }

        cgGenre.setOnCheckedStateChangeListener { group, checkedIds ->
            // if nothing clicked (user unclicked all) then query all posts
            if (checkedIds.isEmpty()) {
                filtered = false
                adapter.submitList(allPosts)
                tvNoPosts.visibility = View.GONE
            }
            // if user clicked some chips then call filtered query on selected genres
            else {
                filtered = true
                val checkedGenres:MutableList<String> = ArrayList()
                for (checkedId in checkedIds) {
                    val chip:Chip? = group.findViewById(checkedId)
                    checkedGenres.add(chip?.text.toString().lowercase())
                }
                filteredQuery(checkedGenres)
            }
        }
    }

    private fun queryPosts() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        query.addDescendingOrder(Post.KEY_LIKES)
        query.fromLocalDatastore().findInBackground().continueWithTask(
            { task: Task<List<Post>?> ->
                // update UI with results from local datastore
                val error = task.error
                if (error == null) {
                    val posts = task.result!!

                    // save received posts to list and notify adapter of new data
                    allPosts.clear()
                    allPosts.addAll(posts)
                    adapter.submitList(allPosts)
                    progressActivity.hideProgressBar()
                    // unpin cache
                    ParseObject.unpinAllInBackground(allPosts)
                }

                // update cache with new query from network
                query.fromNetwork().findInBackground()
            }, ContextCompat.getMainExecutor(requireContext())
        ).continueWithTask(
            { task: Task<List<Post>> ->
                val error = task.error
                if (error == null) {
                    val posts = task.result!!
                    allPosts.clear()
                    allPosts.addAll(posts)

                    // pin all posts to cache
                    ParseObject.pinAllInBackground(allPosts)
                }
                task
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // TODO: performance problems related to song fetch
    private fun search(phrase: String): MutableList<Post> {
        val searchResults: MutableList<Post> = ArrayList()
        if (filtered) {
            for (post in filteredPosts) {
                val song: Song = post.song.fetch() as Song
                if (post.user.username.contains(phrase.lowercase()) || song.songName.lowercase().contains(phrase.lowercase()) || song.artistName.lowercase().contains(phrase.lowercase())) {
                    searchResults.add(post)
                }
            }
        } else {
            for (post in allPosts) {
                val song: Song = post.song.fetch() as Song
                if (post.user.username.contains(phrase.lowercase()) || song.songName.lowercase().contains(phrase.lowercase()) || song.artistName.lowercase().contains(phrase.lowercase())) {
                    searchResults.add(post)
                }
            }
        }
        return searchResults
    }

    // filter posts by genres in chips
    private fun filteredQuery(genres: MutableList<String>) {
        val songQueries: MutableList<ParseQuery<Song>> = ArrayList()

        for (genre in genres) {
            // all songs part of genre
            val songQuery: ParseQuery<Song> = ParseQuery.getQuery(Song::class.java)
            songQuery.whereEqualTo(Song.KEY_GENRE, genre)
            songQueries.add(songQuery)
        }

        val postQuery: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        // get posts where song is a song part of genre
        postQuery.whereMatchesQuery(Post.KEY_SONG, ParseQuery.or(songQueries))
        postQuery.addDescendingOrder(Post.KEY_LIKES)

        postQuery.fromLocalDatastore().findInBackground().continueWithTask(
            { task: Task<List<Post>?> ->
                val error = task.error
                if (error == null) {
                    val posts = task.result!!
                    filteredPosts.clear()
                    filteredPosts.addAll(posts)

                    adapter.submitList(filteredPosts)

                    if (filteredPosts.isEmpty()) {
                        tvNoPosts.visibility = View.VISIBLE
                    } else {
                        tvNoPosts.visibility = View.GONE
                    }

                    ParseObject.unpinAllInBackground(filteredPosts)
                }
                postQuery.fromNetwork().findInBackground()
            }, ContextCompat.getMainExecutor(requireContext())
        ).continueWithTask(
            { task: Task<List<Post>> ->
                val error = task.error
                if (error == null) {
                    val posts = task.result!!
                    filteredPosts.clear()
                    filteredPosts.addAll(posts)

                    ParseObject.pinAllInBackground(filteredPosts)
                }
                task
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    companion object {
        const val TAG = "SearchFragment"
    }
}