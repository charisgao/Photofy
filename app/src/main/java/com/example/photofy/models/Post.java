package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_USER = "User";
    public static final String KEY_IMAGE = "Photo";
    public static final String KEY_SONG = "Song";
    public static final String KEY_CAPTION = "Caption";
    public static final String KEY_CREATED = "createdAt";
    public static final String KEY_LIKES = "Likes";
    public static final String KEY_COMMENTS = "Comments";

    public boolean isLiked;

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseObject getPhoto() {
        return getParseObject(KEY_IMAGE);
    }

    public void setPhoto(ParseObject photo) {
        put(KEY_IMAGE, photo);
    }

    public ParseObject getSong() {
        return getParseObject(KEY_SONG);
    }

    public void setSong(ParseObject song) {
        put(KEY_SONG, song);
    }

    public String getCaption() {
        return getString(KEY_CAPTION);
    }

    public void setCaption(String caption) {
        put(KEY_CAPTION, caption);
    }

    public int getNumLikes() {
        return getInt(KEY_LIKES);
    }

    public int getNumComments() {
        return getInt(KEY_COMMENTS);
    }

    public int updateLikes() {
        if (isLiked) {
            put(KEY_LIKES, getInt(KEY_LIKES) + 1);
        } else {
            put(KEY_LIKES, getInt(KEY_LIKES) - 1);
        }
        try {
            save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return getInt(KEY_LIKES);
    }

    public int updateComments() {
        put(KEY_COMMENTS, getInt(KEY_COMMENTS) + 1);
        try {
            save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getInt(KEY_COMMENTS);
    }
}
