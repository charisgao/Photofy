package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Follow")
public class Follow extends ParseObject {

    public static final String KEY_FROM = "From";
    public static final String KEY_TO = "To";

    public ParseUser getFrom() {
        return getParseUser(KEY_FROM);
    }

    public void setFrom(ParseUser user) {
        put(KEY_FROM, user);
    }

    public ParseUser getTo() {
        return getParseUser(KEY_TO);
    }

    public void setTo(ParseUser user) {
        put(KEY_TO, user);
    }
}
