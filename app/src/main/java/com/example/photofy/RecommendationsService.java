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

import okhttp3.HttpUrl;

/**
 * Recommendations Service using Spotify Web API's GET recommendations endpoint, takes in parameters
 * for a US market, music genre from ColorToGenre and the image, and various parameters depending
 * on user preference
 *
 * Parses JSON response and populates an ArrayList of Song objects for the user to choose from to
 * match their image
 */
public class RecommendationsService {

    public static final String TAG = "RecommendationsService";

    private ArrayList<Song> songs = new ArrayList<>();

    private String token;
    private final RequestQueue queue;

    public RecommendationsService(String token, RequestQueue queue) {
        this.token = token;
        this.queue = queue;
    }

    private String getUrl(String genre, List<String> favGenres){
        HttpUrl.Builder url = HttpUrl.parse("https://api.spotify.com/v1/recommendations").newBuilder();
        url.addQueryParameter("market", "US");
        url.addQueryParameter("seed_genres", genre);

        for (String fav : favGenres) {
            if (fav.equalsIgnoreCase("hip-hop")) {
                url.addQueryParameter("min_energy", "0.4");
            } else if (fav.equalsIgnoreCase("synth-pop")) {
                url.addQueryParameter("min_loudness", "0.4");
            } else if (fav.equalsIgnoreCase("industrial")) {
                url.addQueryParameter("max_acousticness", "0.5");
            } else if (fav.equalsIgnoreCase("classical")) {
                url.addQueryParameter("min_instrumentalness", "0.6");
            } else if (fav.equalsIgnoreCase("ambient")) {
                url.addQueryParameter("max_tempo", "100");
            } else if (fav.equalsIgnoreCase("indie-pop")) {
                url.addQueryParameter("max_energy", "0.6");
            } else if (fav.equalsIgnoreCase("soul")) {
                url.addQueryParameter("min_acousticness", "0.4");
            } else if (fav.equalsIgnoreCase("party")) {
                url.addQueryParameter("min_danceability", "0.75");
            } else if (fav.equalsIgnoreCase("metal")) {
                url.addQueryParameter("min_tempo", "95");
            } else if (fav.equalsIgnoreCase("r-n-b")) {
                url.addQueryParameter("min_valence", "0.4");
            } else if (fav.equalsIgnoreCase("romance")) {
                url.addQueryParameter("max_liveness", "0.7");
            } else if (fav.equalsIgnoreCase("sad")) {
                url.addQueryParameter("max_valence", "0.6");
            } else if (fav.equalsIgnoreCase("grindcore")) {
                url.addQueryParameter("max_loudness", "0.6");
            } else if (fav.equalsIgnoreCase("alternative")) {
                url.addQueryParameter("min_liveness", "0.6");
            }
        }

        Log.d(TAG, url.build().toString());
        return url.build().toString();
    }

    public void getRecommendations(String genre, List<String> favGenres, RecommendationsCallback recommendationsCallback, RecommendationsErrorCallback errorCallback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrl(genre, favGenres), null, new Response.Listener<JSONObject>() {
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
            public Map<String, String> getHeaders() {
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
        if (previewUrl == null) {
            return Optional.empty();
        }
        String spotifyId = object.getString("id");
        String songName = object.getString("name");
        String artistName = object.getJSONArray("artists").getJSONObject(0).getString("name");
        String albumName = object.getJSONObject("album").getString("name");
        String albumUrl = object.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
        int duration = object.getInt("duration_ms");

        Song song = new Song();
        song.setSpotifyId(spotifyId);
        song.setSongName(songName);
        song.setArtistName(artistName);
        song.setAlbumName(albumName);
        song.setAlbumCover(albumUrl);
        song.setGenre(genre);
        song.setPreview(previewUrl);
        song.setDuration(duration);
        Log.i(TAG, "adding song " + songName);
        return Optional.of(song);
    }
}