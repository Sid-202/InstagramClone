package com.codepath.sid_instagram_clone;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Post.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("04BDO7XRPisNjmrFh2toN4o48MEvuOShbrtcMDy8")
                .clientKey("bmKH8vmMmADx9xUKlO9250030pc0Dom94Sdd4Yeo")
                .server("https://parseapi.back4app.com")
                .build());
    }
}
