package com.example.photofy;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.photofy.models.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

public class RecommendationsService {

    public static final String TAG = "RecommendationsService";

    private StringBuilder endpoint = new StringBuilder("https://api.spotify.com/v1/recommendations?market=US");
    private ArrayList<Song> songs = new ArrayList<>();

    private String token;
    private final RequestQueue queue;

    public RecommendationsService(String token, RequestQueue queue) {
        this.token = token;
        this.queue = queue;
    }

    public void getRecommendations(String genre, RecommendationsCallback recommendationsCallback, RecommendationsErrorCallback errorCallback) {
        endpoint.append("&seed_genres=" + genre);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "in response");
                try {
                    JSONArray jsonArray = response.getJSONArray("tracks");
                    for (int n = 0; n < jsonArray.length(); n++) {
                        Optional<Song> song = getSong(jsonArray.getJSONObject(n), genre);
                        song.ifPresent(song1 -> songs.add(song1));
                    }
                    recommendationsCallback.callback(songs);
                } catch (JSONException e) {
                    errorCallback.callback("Error with parsing through songs");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error getting songs " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                Log.i(TAG, "trying to get token");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(request);
    }

    private Optional<Song> getSong(JSONObject object, String genre) throws JSONException {
        String previewUrl = object.getString("preview_url");
        if (previewUrl.isEmpty()) {
            return Optional.empty();
        }
        String spotifyId = object.getString("id");
        String songName = object.getString("name");
        String artistName = object.getJSONArray("artists").getJSONObject(0).getString("name");
        String albumName = object.getJSONObject("album").getString("name");
        String albumUrl = object.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");

        Song song = new Song();
        song.setSpotifyId(spotifyId);
        song.setSongName(songName);
        song.setArtist(artistName);
        song.setAlbum(albumName);
        song.setAlbumCover(albumUrl);
        song.setGenres(Arrays.asList(genre));
        song.setPreview(previewUrl);
        Log.i(TAG, "adding song " + songName);
        return Optional.of(song);
    }
}