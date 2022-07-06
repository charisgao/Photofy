package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {

    public static final String KEY_USER = "User";
    public static final String KEY_POST = "Post";
    public static final String KEY_CREATED = "createdAt";
    public static final String KEY_COMMENT = "Comment";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseObject getPost() {
        return getParseObject(KEY_POST);
    }

    public void setPost(ParseObject post) {
        put(KEY_POST, post);
    }

    public String getComment() {
        return getString(KEY_COMMENT);
    }

    public void setComment(String comment) {
        put(KEY_COMMENT, comment);
    }

}