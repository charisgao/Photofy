package com.example.photofy.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    public static final String TAG = "ProfileAdapter";

    private Context context;
    private List<Post> posts;

    public ProfileAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
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
        private ImageView ivTakenImage;
        private ImageView ivGeneratedSong;
        private TextView tvGeneratedSongName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTakenImage = itemView.findViewById(R.id.ivTakenImage);
            ivGeneratedSong = itemView.findViewById(R.id.ivGeneratedSong);
            tvGeneratedSongName = itemView.findViewById(R.id.tvGeneratedSongName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Post post = posts.get(position);

                        Intent i = new Intent(context, SearchDetailsActivity.class);
                        i.putExtra("post", post);

                        Pair<View, String> p1 = Pair.create(ivTakenImage, "image");
                        Pair<View, String> p2 = Pair.create(tvGeneratedSongName, "songName");
                        Pair<View, String> p3 = Pair.create(ivGeneratedSong, "song");

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, p1, p2, p3);
                        context.startActivity(i, options.toBundle());
                    }
                }
            });
        }

        public void bind(Post post) {
            Photo photo = (Photo) post.getPhoto();
            Song song = (Song) post.getSong();

            photo.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    Glide.with(context).load(photo.getImage().getUrl()).centerCrop().into(ivTakenImage);
                }
            });
            song.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    Glide.with(context).load(song.getAlbumCover()).centerCrop().into(ivGeneratedSong);
                    tvGeneratedSongName.setText(song.getSongName());
                }
            });
        }
    }
}
