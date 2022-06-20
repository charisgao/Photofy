package com.example.photofy;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.photofy.models.Image;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    private String clientKey;

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Image.class);

        try {
            ApplicationInfo applicationInfo = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            clientKey = bundle.getString("clientKey");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("zWEATxbbLsSFXeCWqTMXKWP0j2akWwV9cVZ86Q3p")
                .clientKey(clientKey)
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
