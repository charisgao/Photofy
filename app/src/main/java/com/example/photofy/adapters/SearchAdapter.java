package com.example.photofy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
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

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivSearchImage;
        private TextView tvSearchSongName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSearchImage = itemView.findViewById(R.id.ivSearchImage);
            tvSearchSongName = itemView.findViewById(R.id.tvSearchSongName);
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

                //TODO: click on image to go to view pager
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
