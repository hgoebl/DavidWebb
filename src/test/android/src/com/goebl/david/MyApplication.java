package com.goebl.david;

import android.app.Application;
import android.os.Build;

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
        if (isEmulator()) {
            System.setProperty("EMULATOR", "1");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public static boolean isEmulator() {
        return Build.HARDWARE.contains("goldfish")
                || Build.PRODUCT.equals("sdk")       // sdk
                || Build.PRODUCT.endsWith("_sdk")    // google_sdk
                || Build.PRODUCT.startsWith("sdk_")  // sdk_x86
                || Build.FINGERPRINT.contains("generic");
    }

}
