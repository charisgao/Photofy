package com.example.photofy;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.photofy.models.Comment;
import com.example.photofy.models.Follow;
import com.example.photofy.models.Like;
import com.example.photofy.models.Photo;
import com.example.photofy.models.Post;
import com.example.photofy.models.Song;
import com.parse.Parse;
import com.parse.ParseObject;

public class PhotofyApplication extends Application {

    public static final String TAG = "PhotofyApplication";

    private String clientKey;
    public static String spotifyKey;
    public static String googleCredentials;
    public static String firebaseKey;
    final String APP_ID = "zWEATxbbLsSFXeCWqTMXKWP0j2akWwV9cVZ86Q3p";
    final String SERVER = "https://parseapi.back4app.com";

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Photo.class);
        ParseObject.registerSubclass(Song.class);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Like.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(Follow.class);

        try {
            ApplicationInfo applicationInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            clientKey = bundle.getString("clientKey");
            spotifyKey = bundle.getString("spotifyClientId");
            googleCredentials = bundle.getString("googleCredentials");
            firebaseKey = bundle.getString("firebaseKey");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Client key not found " + e);
        }

        Parse.initialize(new Parse.Configuration.Builder(this)
                .enableLocalDataStore()
                .applicationId(APP_ID)
                .clientKey(clientKey)
                .server(SERVER)
                .build()
        );
    }
}
