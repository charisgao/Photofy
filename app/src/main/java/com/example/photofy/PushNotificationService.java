package com.example.photofy;

import static com.example.photofy.PhotofyApplication.firebaseKey;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.photofy.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Notification Service using Firebase Cloud Messaging, generates notifications to user's devices
 * with a custom NotificationChannel and JsonObjectRequest that creates the appropriate notif
 */
public class PushNotificationService extends FirebaseMessagingService {

    public static final String TAG = "PushNotificationService";
    public static final String CHANNEL_ID = "NotificationChannel";
    public static final String CHANNEL_NAME = "com.example.photofy";
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.i(TAG, "received message");

        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            generateNotification(title, body);
        }
    }

    // generate the notification
    private void generateNotification(String title, String body) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.splash_logo)
                .setAutoCancel(true)
                .setVibrate(new long[] {1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        manager.notify(0, builder.build());
    }

    public static void pushNotification(Context context, String deviceToken, String title, String body) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            JSONObject json = new JSONObject();
            json.put("to", deviceToken);
            json.put("content_available", true);
            json.put("priority", "high");
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);
            json.put("notification", notification);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, "sent " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + firebaseKey);
                    return headers;
                }
            };
            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            Log.e(TAG, "JSONException " + e);
        }
    }
}
