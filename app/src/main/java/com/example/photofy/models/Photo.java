package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Image")
public class Photo extends ParseObject {

    public static final String KEY_IMAGE = "Photo";
    public static final String KEY_USER = "User";
    public static final String KEY_COLOR = "Color";

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public String getColor() {
        return getString(KEY_COLOR);
    }

    public void setColor(String hexColor) {
        put(KEY_COLOR, hexColor);
    }
}
