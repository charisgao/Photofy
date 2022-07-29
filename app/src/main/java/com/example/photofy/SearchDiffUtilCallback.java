package com.example.photofy;

import androidx.recyclerview.widget.DiffUtil;

import com.example.photofy.models.Post;

import java.util.List;

public class SearchDiffUtilCallback extends DiffUtil.Callback {

    private final List<Post> oldPostList;
    private final List<Post> newPostList;

    public SearchDiffUtilCallback(List<Post> oldPostList, List<Post> newPostList) {
        this.oldPostList = oldPostList;
        this.newPostList = newPostList;
    }

    @Override
    public int getOldListSize() {
        return oldPostList.size();
    }

    @Override
    public int getNewListSize() {
        return newPostList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final Post oldP = oldPostList.get(oldItemPosition);
        final Post newP = newPostList.get(newItemPosition);
        return oldP.getId() != null && oldP.getId().equals(newP.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Post oldP = oldPostList.get(oldItemPosition);
        final Post newP = newPostList.get(newItemPosition);
        return oldP.equals(newP);
    }
}
