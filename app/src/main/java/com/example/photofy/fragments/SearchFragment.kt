package com.example.photofy.fragments

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photofy.R
import com.example.photofy.adapters.SearchAdapter
import com.example.photofy.models.Post
import com.example.photofy.models.Song
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.parse.*


class SearchFragment : Fragment() {

    // UI components
    private lateinit var etSearch: EditText
    private lateinit var ivFilter: ImageView
    private lateinit var cgGenre: ChipGroup
    private lateinit var tvNoPosts: TextView
    private lateinit var rvSearchedPosts: RecyclerView

    // variables for filter and search
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

        // bind UI views
        etSearch = view.findViewById(R.id.etSearch)
        ivFilter = view.findViewById(R.id.ivFilter)
        cgGenre = view.findViewById(R.id.cgGenre)
        tvNoPosts = view.findViewById(R.id.tvNoPosts)
        rvSearchedPosts = view.findViewById(R.id.rvSearchedPosts)

        cgGenre.visibility = View.GONE
        tvNoPosts.visibility = View.GONE

        allPosts = ArrayList()
        filteredPosts = ArrayList()

        // create adapter to initially load all posts
        adapter = SearchAdapter(context, allPosts)
        rvSearchedPosts.adapter = adapter
        rvSearchedPosts.layoutManager = GridLayoutManager(context, 2)
        queryPosts()

        etSearch.setOnKeyListener { _, keyCode, event ->
            // user presses enter in the search bar
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                val searchPhrase: String = etSearch.text.toString()
                callAdapter(search(searchPhrase))
                Toast.makeText(context, etSearch.text, Toast.LENGTH_SHORT).show()
                etSearch.setText("")
            }
            false
        }

        // filter button
        var pressed = false
        ivFilter.setOnClickListener {
            pressed = !pressed
            if (pressed) {
                cgGenre.visibility = View.VISIBLE
            } else {
                cgGenre.visibility = View.GONE
            }
        }

        // chips
        cgGenre.setOnCheckedStateChangeListener { group, checkedIds ->
            // if nothing clicked (user unclicked all) then query all posts
            if (checkedIds.isEmpty()) {
                filtered = false
                callAdapter(allPosts)
            }
            // if user clicked some chips then call filtered query
            else {
                filtered = true
                var checkedGenres:MutableList<String> = ArrayList()
                for (checkedId in checkedIds) {
                    val chip:Chip? = group.findViewById(checkedId)
                    checkedGenres.add(chip?.text.toString().lowercase())
                }
                filteredQuery(checkedGenres)
            }
        }
    }

    // get all posts
    private fun queryPosts() {
        allPosts.clear()
        val query = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        query.addDescendingOrder(Post.KEY_CREATED)
        query.findInBackground(object : FindCallback<Post> {
            override fun done(posts: List<Post>, e: ParseException?) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e)
                    return
                }

                allPosts.addAll(posts)
                adapter.notifyDataSetChanged()
            }
        })
    }

    // new adapter to update data given posts
    private fun callAdapter(posts: List<Post>) {
        adapter = SearchAdapter(
            context,
            posts)
        rvSearchedPosts.adapter = adapter
        rvSearchedPosts.layoutManager = GridLayoutManager(context, 2)
        adapter.notifyDataSetChanged()
    }

    // search for posts
    private fun search(phrase: String): MutableList<Post> {
        var searchResults: MutableList<Post> = ArrayList()
        if (filtered) {
            for (post in filteredPosts) {
                if (post.caption.lowercase().contains(phrase.lowercase())) {
                    searchResults.add(post)
                }
            }
        } else {
            for (post in allPosts) {
                if (post.caption.lowercase().contains(phrase.lowercase())) {
                    searchResults.add(post)
                }
            }
        }
        return searchResults
    }

    // filter posts by genres in chips
    private fun filteredQuery(genres: MutableList<String>) {
        filteredPosts.clear()
        var songQueries: MutableList<ParseQuery<Song>> = ArrayList()

        for (genre in genres) {
            // all songs part of genre
            val songQuery: ParseQuery<Song> = ParseQuery.getQuery(Song::class.java)
            songQuery.whereEqualTo(Song.KEY_GENRE, genre)
            songQueries.add(songQuery)
        }

        val postQuery: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        // get posts where song is a song part of genre
        postQuery.whereMatchesQuery(Post.KEY_SONG, ParseQuery.or(songQueries))
        postQuery.addDescendingOrder(Post.KEY_CREATED)
        postQuery.findInBackground(object : FindCallback<Post> {
            override fun done(posts: List<Post>, e: ParseException?) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with filtering posts", e)
                    return
                }
                filteredPosts.addAll(posts)
                callAdapter(filteredPosts)
                if (filteredPosts.isEmpty()) {
                    tvNoPosts.visibility = View.VISIBLE
                } else {
                    tvNoPosts.visibility = View.GONE
                }
            }
        })
    }

    companion object {
        const val TAG = "SearchFragment"
    }
}