package com.example.photofy.fragments

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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

    private lateinit var etSearch: EditText
    private lateinit var ivFilter: ImageView
    private lateinit var cgGenre: ChipGroup

    private lateinit var rvSearchedPosts: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var adapter: SearchAdapter
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

        etSearch = view.findViewById(R.id.etSearch)
        ivFilter = view.findViewById(R.id.ivFilter)
        cgGenre = view.findViewById(R.id.cgGenre)
        cgGenre.visibility = View.GONE

        rvSearchedPosts = view.findViewById(R.id.rvSearchedPosts)
        filteredPosts = java.util.ArrayList()
        adapter = SearchAdapter(
            context,
            filteredPosts)
        rvSearchedPosts.adapter = adapter
        gridLayoutManager = GridLayoutManager(context, 2)
        rvSearchedPosts.layoutManager = gridLayoutManager

        queryPosts()

        etSearch.setText("")
        etSearch.setOnKeyListener { v, keyCode, event ->
            // user presses enter in the search bar
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                val searchPhrase: String = etSearch.text.toString()
                val searchResults = search(searchPhrase)
                adapter.clear()
                filteredPosts.addAll(searchResults)
                adapter.notifyDataSetChanged()
                Toast.makeText(context, etSearch.text, Toast.LENGTH_SHORT).show()
            }
            false
        }

        var pressed = false

        ivFilter.setOnClickListener { _ ->
            pressed = !pressed
            if (pressed) {
                cgGenre.visibility = View.VISIBLE
            } else {
                cgGenre.visibility = View.GONE
            }
        }

        cgGenre.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                filtered = false
                adapter.clear()
                queryPosts()
            } else {
                filtered = true
                var checkedGenres:MutableList<String> = ArrayList<String>()
                for (checkedId in checkedIds) {
                    val chip:Chip? = group.findViewById(checkedId)
                    checkedGenres.add(chip?.text.toString().lowercase())
                }
                adapter.clear()
                filteredQuery(checkedGenres)
            }
        }
    }

    private fun queryPosts() {
        val query = ParseQuery.getQuery(Post::class.java)
        query.include(Post.KEY_USER)
        query.addDescendingOrder(Post.KEY_CREATED)
        query.findInBackground(object : FindCallback<Post> {
            override fun done(posts: List<Post>, e: ParseException?) {
                // Check for errors
                if (e != null) {
                    Log.e(SearchFragment.TAG, "Issue with getting posts", e)
                    return
                }

                filteredPosts.addAll(posts)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun search(phrase: String): MutableList<Post> {
        var searchResults: MutableList<Post> = ArrayList<Post>()
        if (filtered) {
            // TODO: use filtered list
            for (post in filteredPosts) {
                if (post.caption.lowercase().contains(phrase.lowercase())) {
                    searchResults.add(post)
                }
            }
//        } else {
//            for (post in allPosts) {
//                if (post.caption.lowercase().contains(phrase)) {
//                    searchResults.add(post)
//                }
//            }
        }
        return searchResults
    }

    private fun filteredQuery(genres: MutableList<String>) {
        var songQueries: MutableList<ParseQuery<Song>> = ArrayList<ParseQuery<Song>>()

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
                    Log.e(SearchFragment.TAG, "Issue with filtering posts", e)
                    return
                }

                // Save received posts to list and notify adapter of new data
                filteredPosts.addAll(posts)
                adapter.notifyDataSetChanged()
            }
        })
    }

    companion object {
        const val TAG = "SearchFragment"
    }
}