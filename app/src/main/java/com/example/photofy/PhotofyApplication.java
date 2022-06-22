package com.example.photofy;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.photofy.models.Photo;
import com.parse.Parse;
import com.parse.ParseObject;

public class PhotofyApplication extends Application {

    public static final String TAG = "PhotofyApplication";

    private String clientKey;
    public static String spotifyKey;
    public static String googleCredentials;
    final String APP_ID = "zWEATxbbLsSFXeCWqTMXKWP0j2akWwV9cVZ86Q3p";
    final String SERVER = "https://parseapi.back4app.com";

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Photo.class);

        try {
            ApplicationInfo applicationInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            clientKey = bundle.getString("clientKey");
            spotifyKey = bundle.getString("spotifyClientId");
            googleCredentials = bundle.getString("googleCredentials");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Client key not found " + e);
        }

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APP_ID)
                .clientKey(clientKey)
                .server(SERVER)
                .build()
        );
    }
}
