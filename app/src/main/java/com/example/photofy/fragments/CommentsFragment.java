package com.example.photofy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.photofy.adapters.CommentsAdapter;
import com.example.photofy.R;
import com.example.photofy.models.Comment;
import com.example.photofy.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {

    public static final String TAG = "CommentsFragment";

    protected CommentsAdapter commentsAdapter;
    protected List<Comment> comments;

    private RecyclerView rvComments;
    private EditText etAddComment;
    private TextView tvTotalNumComments;
    private ImageButton ibSendComment;
    private ImageButton ibClose;

    private Post post;

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvComments = view.findViewById(R.id.rvComments);
        etAddComment = view.findViewById(R.id.etAddComment);
        tvTotalNumComments = view.findViewById(R.id.tvTotalNumComments);
        ibSendComment = view.findViewById(R.id.ibSendComment);
        ibClose = view.findViewById(R.id.ibClose);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getContext(), comments);

        rvComments.setAdapter(commentsAdapter);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            post = bundle.getParcelable("post");

            int count = post.getNumComments();
            if (count == 1) {
                tvTotalNumComments.setText(String.valueOf(count + " comment"));
            } else {
                tvTotalNumComments.setText(String.valueOf(count + " comments"));
            }
        }

        fillComments();

        ibSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newComment();

                int count = post.updateComments();
                if (count == 1) {
                    tvTotalNumComments.setText(String.valueOf(count + " comment"));
                } else {
                    tvTotalNumComments.setText(String.valueOf(count + " comments"));
                }
            }
        });

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void fillComments() {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_USER);
        query.whereEqualTo(Comment.KEY_POST, post);
        query.addDescendingOrder(Comment.KEY_CREATED);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                // Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments " + e);
                    return;
                }
                comments.addAll(objects);
                commentsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void newComment() {
        Comment comment = new Comment();
        comment.setUser(ParseUser.getCurrentUser());
        comment.setPost(post);
        comment.setComment(etAddComment.getText().toString());
        comment.saveInBackground();
        etAddComment.setText("");

        comments.add(0, comment);
        commentsAdapter.notifyItemInserted(0);
        rvComments.smoothScrollToPosition(0);
    }
}