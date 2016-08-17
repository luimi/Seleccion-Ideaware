package com.lui2mi.testideaware;

import android.app.Application;

import utils.ObjectManager;

/**
 * Created by lui2mi on 16/08/16.
 */
public class App extends Application {
    /**
     * Made this object to be global, but it should have a better logic
     */
    public static ObjectManager data;

    @Override
    public void onCreate() {
        super.onCreate();
        data=new ObjectManager(this);
    }
}
