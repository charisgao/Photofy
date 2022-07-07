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
import com.example.photofy.models.Song;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    public static final String TAG = "SongAdapter";
    private Context context;
    private List<Song> songs;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivSongAlbumCover;
        private TextView tvRecommendedSong;
        private TextView tvRecommendedArtist;
        private TextView tvRecommendedAlbum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSongAlbumCover = itemView.findViewById(R.id.ivSongAlbumCover);
            tvRecommendedSong = itemView.findViewById(R.id.tvRecommendedSong);
            tvRecommendedArtist = itemView.findViewById(R.id.tvRecommendedArtist);
            tvRecommendedAlbum = itemView.findViewById(R.id.tvRecommendedAlbum);
        }

        public void bind(Song song) {
            // Bind the song data to the view elements
            Glide.with(context).load(song.getAlbumCover()).into(ivSongAlbumCover);
            tvRecommendedSong.setText(song.getSongName());
            tvRecommendedArtist.setText(song.getArtistName());
            tvRecommendedAlbum.setText(song.getAlbumName());
        }
    }
}
