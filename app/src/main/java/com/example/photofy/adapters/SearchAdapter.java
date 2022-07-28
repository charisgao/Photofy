package com.example.photofy.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Explode;

import com.bumptech.glide.Glide;
import com.example.photofy.DetailsTransition;
import com.example.photofy.R;
import com.example.photofy.SearchDiffUtilCallback;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.fragments.SearchDetailsFragment;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{

    public static final String TAG = "SearchAdapter";
    private final Context context;
    private List<Post> posts;
    private RecyclerView rvSearchedPosts;

    public SearchAdapter(Context context, List<Post> posts, RecyclerView rvSearchedPosts) {
        this.context = context;
        this.posts = posts;
        this.rvSearchedPosts = rvSearchedPosts;
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

    public void submitList(List<Post> newList) {
        List<Post> oldList = posts;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SearchDiffUtilCallback(oldList, newList));
        posts = newList;
        diffResult.dispatchUpdatesTo(this);
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

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            RenderEffect blurEffect = RenderEffect.createBlurEffect(16, 16, Shader.TileMode.MIRROR);
                            rvSearchedPosts.setRenderEffect(blurEffect);
                        }

                        Bundle bundle = new Bundle();
                        bundle.putParcelable("post", post);

                        SearchDetailsFragment searchDetailsFragment = new SearchDetailsFragment();

                        searchDetailsFragment.setSharedElementEnterTransition(new DetailsTransition());
                        searchDetailsFragment.setEnterTransition(new Explode());

                        searchDetailsFragment.setArguments(bundle);
                        ((MainActivity) context).getSupportFragmentManager()
                                .beginTransaction()
                                .addSharedElement(ivSearchImage, "detailsImage")
                                .replace(R.id.flSearchDetails, searchDetailsFragment)
                                .commit();
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
