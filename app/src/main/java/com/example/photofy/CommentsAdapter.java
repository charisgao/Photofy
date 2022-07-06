package com.example.photofy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofy.models.Comment;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    public static final String TAG = "CommentsAdapter";
    private Context context;
    private List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivCommentPicture;
        private TextView tvCommentUsername;
        private TextView tvComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCommentPicture = itemView.findViewById(R.id.ivCommentPicture);
            tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
        }

        public void bind(Comment comment) {
            // TODO: load profile picture
            tvCommentUsername.setText(comment.getUser().getUsername());
            tvComment.setText(comment.getComment());
        }
    }
}
