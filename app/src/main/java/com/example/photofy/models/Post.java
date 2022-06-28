package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_USER = "User";
    public static final String KEY_IMAGE = "Photo";
    public static final String KEY_SONG = "Song";
    public static final String KEY_CAPTION = "Caption";

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
}
