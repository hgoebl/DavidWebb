package com.goebl.david;

import android.app.Application;

import java.io.File;

/**
 * Application to set a few system properties.
 *
 * @author hgoebl
 * @since 22.01.14
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File cacheDir = getApplicationContext().getCacheDir();
        System.setProperty("CACHE_DIR", cacheDir.getAbsolutePath());
        System.setProperty("ANDROID", "1");
    }
}
