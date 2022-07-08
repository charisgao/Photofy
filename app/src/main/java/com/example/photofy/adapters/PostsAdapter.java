package com.example.photofy.adapters;

import static com.example.photofy.fragments.HomeFragment.mSpotifyAppRemote;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofy.R;
import com.example.photofy.activities.MainActivity;
import com.example.photofy.fragments.CommentsFragment;
import com.example.photofy.fragments.ProfileFragment;
import com.example.photofy.models.Like;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.PlayerState;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>{

    public static final String TAG = "PostsAdapter";
    private final Context context;
    private final List<Post> posts;
    private final NavController navController;

    public PostsAdapter(Context context, List<Post> posts, NavController navController) {
        this.context = context;
        this.posts = posts;
        this.navController = navController;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(getItemViewType(position));
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImage;

        private final ImageView ivProfile;
        private final TextView tvUsername;
        private final TextView tvCaption;
        private final ImageButton ibLike;
        private final ImageButton ibComment;
        private final TextView tvNumLikes;
        private final TextView tvNumComments;

        private final TextView tvSongName;
        private final SeekBar seekBar;
        private final TextView tvTime;
        private final ImageButton ibPlay;

        private String duration;
        private ScheduledExecutorService timer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.ivImage);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            ibLike = itemView.findViewById(R.id.ibLike);
            ibComment = itemView.findViewById(R.id.ibComment);
            tvNumLikes = itemView.findViewById(R.id.tvNumLikes);
            tvNumComments = itemView.findViewById(R.id.tvNumComments);

            tvSongName = itemView.findViewById(R.id.tvSongName);
            seekBar = itemView.findViewById(R.id.seekBar);
            tvTime = itemView.findViewById(R.id.tvTime);
            ibPlay = itemView.findViewById(R.id.ibPlay);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                boolean firstTouch = false;
                long time = System.currentTimeMillis();
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int position = getAdapterPosition();
                    Post post = posts.get(position);
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        if(firstTouch && (System.currentTimeMillis() - time) <= 300) {
                            if (post.isLiked) {
                                unlikePost(post);
                                ibLike.setImageResource(R.drawable.ufi_heart);
                            } else {
                                likePost(post);
                                ibLike.setImageResource(R.drawable.ufi_heart_active);
                            }
                            post.isLiked = !post.isLiked;
                            int count = post.updateLikes();
                            tvNumLikes.setText(Integer.toString(count));
                            Log.i(TAG, "double tap");
                            firstTouch = false;
                        } else {
                            firstTouch = true;
                            time = System.currentTimeMillis();
                            Log.i(TAG, "single tap " + time);
                            return false;
                        }
                    }
                    return true;
                }
            });
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

            bindLikeButton(post);

            tvNumLikes.setText(Integer.toString(post.getNumLikes()));
            tvNumComments.setText(Integer.toString(post.getNumComments()));

            ibLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (post.isLiked) {
                        unlikePost(post);
                        ibLike.setImageResource(R.drawable.ufi_heart);
                    } else {
                        likePost(post);
                        ibLike.setImageResource(R.drawable.ufi_heart_active);
                    }
                    post.isLiked = !post.isLiked;
                    int count = post.updateLikes();
                    tvNumLikes.setText(Integer.toString(count));
                }
            });

            ibComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("post", post);
                    navController.navigate(R.id.action_blankFragment_to_commentsFragment, bundle);
                }
            });

            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goOtherProfile(post);
                }
            });

            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goOtherProfile(post);
                }
            });

            Song song = (Song) post.getSong();
            song.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    tvSongName.setText(song.getSongName());
                    setupSeekBar(song.getDuration());
                }
            });

            timer = Executors.newScheduledThreadPool(1);
            timer.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>() {
                        @Override
                        public void onResult(PlayerState status) {
                            if (!seekBar.isPressed()) {
                                seekBar.setProgress((int) status.playbackPosition);
                                Log.d(TAG, "" + status.playbackPosition);
                            }
                        }
                    });
                }
            }, 10, 100, TimeUnit.MILLISECONDS);

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
                                } else {
                                    mSpotifyAppRemote.getPlayerApi().resume();
                                }
                                ibPlay.setImageResource(R.drawable.ic_pause_button);
                                count++;
                            } else {
                                mSpotifyAppRemote.getPlayerApi().pause();
                                ibPlay.setImageResource(R.drawable.ic_play_button);
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
                        public void onResult(PlayerState status) {
                            int millis = (int) status.playbackPosition;
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

        private void bindLikeButton(Post post) {
            ParseQuery<Like> query = ParseQuery.getQuery(Like.class);

            // See if post is liked by the current user
            query.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());
            query.whereEqualTo(Like.KEY_POST, post);

            query.findInBackground(new FindCallback<Like>() {
                @Override
                public void done(List<Like> objects, ParseException e) {
                    // Check for errors
                    if (e != null) {
                        Log.e(TAG, "Issue with getting likes " + e);
                        return;
                    }
                    if (!objects.isEmpty()) {
                        post.isLiked = true;
                        ibLike.setImageResource(R.drawable.ufi_heart_active);
                    } else {
                        post.isLiked = false;
                        ibLike.setImageResource((R.drawable.ufi_heart));
                    }
                }
            });
        }

        private void likePost(Post post) {
            Like like = new Like();
            like.setUser(ParseUser.getCurrentUser());
            like.setPost(post);
            like.saveInBackground();
        }

        private void unlikePost(Post post) {
            ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
            query.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());
            query.whereEqualTo(Like.KEY_POST, post);

            query.findInBackground(new FindCallback<Like>() {
                @Override
                public void done(List<Like> objects, ParseException e) {
                    // Check for errors
                    if (e != null) {
                        Log.e(TAG, "Issue with finding like to delete " + e);
                        return;
                    }
                    if (!objects.isEmpty()) {
                        objects.get(0).deleteInBackground();
                    }
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

        private void goOtherProfile(Post post) {
            ProfileFragment otherProfileFragment = new ProfileFragment(post.getUser());
            FragmentTransaction transaction =((MainActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.flContainer, otherProfileFragment).addToBackStack(null).commit();
        }
    }
}
