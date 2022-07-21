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

public class RecommendationsService {

    public static final String TAG = "RecommendationsService";

    private ArrayList<Song> songs = new ArrayList<>();

    private String token;
    private final RequestQueue queue;

    public RecommendationsService(String token, RequestQueue queue) {
        this.token = token;
        this.queue = queue;
    }

    private String getUrl(String genre, String parameter){
        HttpUrl.Builder url = HttpUrl.parse("https://api.spotify.com/v1/recommendations").newBuilder();
        url.addQueryParameter("market", "US");
        url.addQueryParameter("seed_genres", genre);
        char type1 = parameter.charAt(0);
        char type2 = parameter.charAt(1);
        char type3 = parameter.charAt(2);
        if (type1 == 'h' || type2 == 'h' || type3 == 'h') { // amusing, hip-hop
            url.addQueryParameter("min_energy", "0.4");
        } if (type1 == 's' || type2 == 's' || type3 == 's') { // annoyed, synth-pop
            url.addQueryParameter("min_loudness", "0.4");
        } if (type1 == 'i' || type2 == 'i' || type3 == 'i') { // anxious, industrial
            url.addQueryParameter("max_acousticness", "0.5");
        } if (type1 == 'c' || type2 == 'c' || type3 == 'c') { // beautiful, classical
            url.addQueryParameter("min_instrumentalness", "0.6");
        } if (type1 == 'a' || type2 == 'a' || type3 == 'a') { // calm, ambient
            url.addQueryParameter("max_tempo", "100");
        } if (type1 == 'n' || type2 == 'n' || type3 == 'n') { // desirous, indie-pop
            url.addQueryParameter("max_energy", "0.6");
        } if (type1 == 'o' || type2 == 'o' || type3 == 'o') { // dreamy, soul
            url.addQueryParameter("min_acousticness", "0.4");
        } if (type1 == 'p' || type2 == 'p' || type3 == 'p') { // energizing, party
            url.addQueryParameter("min_danceability", "0.75");
        } if (type1 == 'm' || type2 == 'm' || type3 == 'm') { // fear, metal
            url.addQueryParameter("min_tempo", "95");
        } if (type1 == 'r' || type2 == 'r' || type3 == 'r') { // joyful, r-n-b
            url.addQueryParameter("min_valence", "0.4");
        } if (type1 == 'e' || type2 == 'e' || type3 == 'e') { // loving, romance
            url.addQueryParameter("max_liveness", "0.7");
        } if (type1 == 'S' || type2 == 'S' || type3 == 'S') { // sad
            url.addQueryParameter("max_valence", "0.6");
        } if (type1 == 'g' || type2 == 'g' || type3 == 'g') { // scary, grindcore
            url.addQueryParameter("max_loudness", "0.6");
        } if (type1 == 'l' || type2 == 'l' || type3 == 'l') { // triumphant, alternative
            url.addQueryParameter("min_liveness", "0.6");
        }
        Log.d(TAG, url.build().toString());
        return url.build().toString();
    }

    public void getRecommendations(String genre, String parameter, RecommendationsCallback recommendationsCallback, RecommendationsErrorCallback errorCallback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrl(genre, parameter), null, new Response.Listener<JSONObject>() {
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