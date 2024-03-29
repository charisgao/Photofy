package com.example.photofy.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Song")
public class Song extends ParseObject {

    public static final String KEY_OBJECT_ID = "objectId";
    public static final String KEY_SPOTIFYID = "SpotifyId";
    public static final String KEY_NAME = "Name";
    public static final String KEY_ARTIST = "Artist";
    public static final String KEY_ALBUM = "Album";
    public static final String KEY_ALBUM_COVER = "AlbumCover";
    public static final String KEY_GENRE = "Genres";
    public static final String KEY_PREVIEW = "Preview";
    public static final String KEY_DURATION = "Duration";

    public String getId() {
        return getString(KEY_OBJECT_ID);
    }

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

    public String getArtistName() {
        return getString(KEY_ARTIST);
    }

    public void setArtistName(String artist) {
        put(KEY_ARTIST, artist);
    }

    public String getAlbumName() {
        return getString(KEY_ALBUM);
    }

    public void setAlbumName(String album) {
        put(KEY_ALBUM, album);
    }

    public String getAlbumCover() {
        return getString(KEY_ALBUM_COVER);
    }

    public void setAlbumCover(String albumUrl) {
        put(KEY_ALBUM_COVER, albumUrl);
    }

    public String getGenre() {
        return getString(KEY_GENRE);
    }

    public void setGenre(String genre) {
        put(KEY_GENRE, genre);
    }

    public String getPreview() {
        return getString(KEY_PREVIEW);
    }

    public void setPreview(String preview) {
        put(KEY_PREVIEW, preview);
    }

    public int getDuration() {
        return getInt(KEY_DURATION);
    }

    public void setDuration(int duration) {
        put(KEY_DURATION, duration);
    }
}
