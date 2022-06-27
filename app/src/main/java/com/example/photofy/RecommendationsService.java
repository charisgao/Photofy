package com.example.photofy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.photofy.models.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationsService {

    public static final String TAG = "RecommendationsService";

    private StringBuilder endpoint = new StringBuilder("https://api.spotify.com/v1/recommendations?market=US");
    private List<Song> songs = new ArrayList<>();

    private String genre;
    private final SharedPreferences sharedPreferences;
    private final RequestQueue queue;

    public RecommendationsService(Context context, String genre) {
        this.genre = genre;
        endpoint.append("&seed_genres=" + genre);
        sharedPreferences = context.getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(context);
        getRecommendations();
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void getRecommendations() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, endpoint.toString(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "in response");
                try {
                    JSONArray jsonArray = response.getJSONArray("tracks");
                    for (int n = 0; n < jsonArray.length(); n++) {
                        JSONObject object = jsonArray.getJSONObject(n);
                        String previewUrl = object.getString("preview_url");
                        if (previewUrl != null) {
                            String spotifyId = object.getString("id");
                            String songName = object.getString("name");
                            String artistName = object.getJSONArray("artists").getJSONObject(0).getString("name");
                            Song song = new Song();
                            song.setSpotifyId(spotifyId);
                            song.setSongName(songName);
                            song.setArtist(artistName);
                            song.setGenres(Arrays.asList(genre));
                            song.setPreview(previewUrl);
                            Log.i(TAG, "adding song " + songName);
                            songs.add(song);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(request);
    }
}