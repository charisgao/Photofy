package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import java.util.List;

@ParseClassName("Song")
public class Song extends ParseObject {

    public static final String KEY_SPOTIFYID = "SpotifyId";
    public static final String KEY_NAME = "Name";
    public static final String KEY_ARTIST = "Artist";
    public static final String KEY_GENRES = "Genres";
    public static final String KEY_PREVIEW = "Preview";
    public static final String KEY_LINK = "Link";

    public String getSpotifyId() {
        return getString(KEY_SPOTIFYID);
    }

    public void setSpotifyId(String spotifyId) {
        put(KEY_SPOTIFYID, spotifyId);
    }

    public String getSongName() {
        return getString(KEY_NAME);
    }

    public void setSongName(String songName) {
        put(KEY_NAME, songName);
    }

    public String getArtist() {
        return getString(KEY_ARTIST);
    }

    public void setArtist(String artist) {
        put(KEY_ARTIST, artist);
    }

    public List getGenres() {
        return getList(KEY_GENRES);
    }

    public void setGenres(List<String> genres) {
        put(KEY_GENRES, genres);
    }

    public String getPreview() {
        return getString(KEY_PREVIEW);
    }

    public void setPreview(String preview) {
        put(KEY_PREVIEW, preview);
    }

    public String getLink() {
        return getString(KEY_LINK);
    }

    public void setLink(String link) {
        put(KEY_LINK, link);
    }
}
