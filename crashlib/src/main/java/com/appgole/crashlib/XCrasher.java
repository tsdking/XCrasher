package com.appgole.crashlib;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class XCrasher {
    private static final String TAG = "XCrasher";

    public static void init(Application application) {
        init(application, null);
    }

    public static void init(Application application, CrashListener crashListener) {
        if (!XLooper.isSafe()) {
            CrashHandler crashHandler = new CrashHandler(crashListener);
            application.registerActivityLifecycleCallbacks(crashHandler.getActvityLifecycleCallback());
            XLooper.start(crashHandler);
        }
    }

    public static void recover(Activity activity) {
        if (activity != null) {
            try {
                ActivityInfo[] list = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), PackageManager.GET_ACTIVITIES).activities;
                if (list.length == 1 && list[0].name.equals(activity.getClass().getName())) {
                    activity.finish();
                    activity.startActivity(new Intent(activity, activity.getClass()));
                } else {
                    activity.onBackPressed();
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
