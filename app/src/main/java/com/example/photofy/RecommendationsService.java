package com.example.photofy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.photofy.models.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationsService {

    public static final String TAG = "RecommendationsService";

    private static final String ENDPOINT = "https://api.spotify.com/v1/recommendations";
    private List<Song> songs = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;

    public RecommendationsService(Context context, String genre) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(context);

        JSONObject parameters = addParams(genre);
        JsonObjectRequest jsonObjectRequest = getRecommendations(parameters);
        queue.add(jsonObjectRequest);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public JSONObject addParams(String genre) {
        JSONObject params = new JSONObject();
        try {
            params.put("seed_genres", genre);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }

    public JsonObjectRequest getRecommendations(JSONObject params) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ENDPOINT, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "in response");
                JSONArray jsonArray = response.optJSONArray("tracks");
                for (int n = 0; n < jsonArray.length(); n++) {
                    try {
                        JSONObject object = jsonArray.getJSONObject(n);
                        String spotifyId = object.getJSONObject("linked_from").getString("id");
                        Song song = new Song();
                        song.setSpotifyId(spotifyId);
                        songs.add(song);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error getting songs");
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        return request;
    }
}