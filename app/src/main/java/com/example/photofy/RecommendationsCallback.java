package com.example.photofy;

import com.example.photofy.models.Song;

import java.util.ArrayList;

public interface RecommendationsCallback {
    void callback(ArrayList<Song> songs);
}

