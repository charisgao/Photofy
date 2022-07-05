package com.example.photofy;

import static com.example.photofy.fragments.HomeFragment.mSpotifyAppRemote;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.PlayerState;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>{

    public static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
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

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivImage;

        private ImageView ivProfile;
        private TextView tvUsername;
        private TextView tvCaption;

        private TextView tvSongName;
        private SeekBar seekBar;
        private TextView tvTime;
        private ImageButton ibPlay;

        private String duration;
        private MediaPlayer mediaPlayer;
        private ScheduledExecutorService timer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.ivImage);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);

            tvSongName = itemView.findViewById(R.id.tvSongName);
            seekBar = itemView.findViewById(R.id.seekBar);
            tvTime = itemView.findViewById(R.id.tvTime);
            ibPlay = itemView.findViewById(R.id.ibPlay);
        }

        public void bind(Post post) {
            Photo photo = (Photo) post.getPhoto();
            photo.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    Glide.with(context).load(photo.getImage().getUrl()).into(ivImage);
                }
            });

            Glide.with(context).load(post.getUser().getParseFile("Profile").getUrl()).circleCrop().into(ivProfile);
            tvUsername.setText(post.getUser().getUsername());
            tvCaption.setText(post.getCaption());

            Song song = (Song) post.getSong();
            song.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    tvSongName.setText(song.getSongName());
                    song.getSpotifyId();
                    setupSeekBar(song.getDuration());
                }
            });

            ibPlay.setOnClickListener(new View.OnClickListener() {
                int count = 0;
                @Override
                public void onClick(View v) {
                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>() {
                        @Override
                        public void onResult(PlayerState status) {
                            if (status.isPaused) {
                                if (count == 0) {
                                    mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + song.getSpotifyId());
                                    ibPlay.setImageResource(R.drawable.ic_pause_button);
                                    count++;
                                } else {
                                    mSpotifyAppRemote.getPlayerApi().resume();
                                    ibPlay.setImageResource(R.drawable.ic_pause_button);
                                    count++;
                                }

                            timer = Executors.newScheduledThreadPool(1);
                            timer.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    if (!seekBar.isPressed()) {
                                        seekBar.setProgress((int) status.playbackPosition);
                                    }
                                }
                            }, 10, 10, TimeUnit.MILLISECONDS);

                            } else {
                                mSpotifyAppRemote.getPlayerApi().pause();
                                ibPlay.setImageResource(R.drawable.ic_play_button);
                                timer.shutdown();
                            }
                        }
                    });
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>() {
                        @Override
                        public void onResult(PlayerState data) {
                            int millis = (int) data.playbackPosition;
                            long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
                            long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
                            String secs = new DecimalFormat("00").format(total_secs - (mins * 60));
                            tvTime.setText(mins + ":" + secs + " / " + duration);
                        }
                    });
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mSpotifyAppRemote.getPlayerApi().seekTo(seekBar.getProgress());
                }
            });
        }

        private void setupSeekBar(int millis) {
            long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
            long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
            String secs = new DecimalFormat("00").format(total_secs - (mins * 60));
            duration = mins + ":" + secs;
            tvTime.setText("00:00 / " + duration);
            seekBar.setMax(millis);
            seekBar.setProgress(0);
        }
    }
}
