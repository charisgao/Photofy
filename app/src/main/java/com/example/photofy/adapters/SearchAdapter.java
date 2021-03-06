package com.example.photofy.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.example.photofy.activities.SearchDetailsActivity;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{

    public static final String TAG = "SearchAdapter";
    private final Context context;
    private final List<Post> posts;
    private ActivityResultLauncher<Intent> detailsLauncher;

    public SearchAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearSong;
        private ImageView ivSearchImage;
        private TextView tvSearchSongName;
        private TextView tvSearchSongArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearSong = itemView.findViewById(R.id.linearSong);
            ivSearchImage = itemView.findViewById(R.id.ivSearchImage);
            tvSearchSongName = itemView.findViewById(R.id.tvSearchSongName);
            tvSearchSongArtist = itemView.findViewById(R.id.tvSearchSongArtist);
        }

        public void bind(Post post) {
            try {
                Photo photo = post.getPhoto().fetch();
                Song song = post.getSong().fetch();

                ParseFile image = photo.getImage();
                if (image != null) {
                    Glide.with(context).load(image.getUrl()).into(ivSearchImage);
                }
                tvSearchSongName.setText(song.getSongName());
                tvSearchSongArtist.setText(song.getArtistName());

                linearSong.setBackgroundColor(Color.parseColor("#D4" + photo.getColor().substring(1)));

                ivSearchImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "clicked");

                        Intent i = new Intent(context, SearchDetailsActivity.class);
                        i.putExtra("post", post);

                        Pair<View, String> p1 = Pair.create(ivSearchImage, "image");
                        Pair<View, String> p2 = Pair.create(tvSearchSongName, "songName");
                        Pair<View, String> p3 = Pair.create(tvSearchSongArtist, "artistName");

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, p1, p2, p3);
                        context.startActivity(i, options.toBundle());
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
