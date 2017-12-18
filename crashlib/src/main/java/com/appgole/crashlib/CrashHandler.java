package com.appgole.crashlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private Activity currentActivity;
    private CrashListener crashListener;

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;

    public CrashHandler(CrashListener crashListener) {
        this.crashListener = crashListener;
        activityLifecycleCallbacks = new LocalActivityLifecycleCallback();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable throwable) {
        if (currentActivity != null) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (crashListener != null) {
                        crashListener.onCrash(throwable, currentActivity);
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(currentActivity)
                                .setTitle("CRASH")
                                .setMessage(throwable.getMessage())
                                .setPositiveButton("Recover", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        XCrasher.recover(currentActivity);
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                }
            });
            // TODO: 2017/12/18
        }
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        String sb = "";
        if (stackTrace != null && stackTrace.length > 0) {
            for (int i = 0; i < stackTrace.length; i++) {
                sb = sb.concat(stackTrace[i].toString()).concat("\n");
            }
        }
        Log.e(TAG, "uncaughtException_MESSAGE: " + throwable.getMessage() + "\n STACKTRACE:\n" + sb + "\n HAPPENED_THEAD:" + thread.getName());
    }

    public Application.ActivityLifecycleCallbacks getActvityLifecycleCallback() {
        return activityLifecycleCallbacks;
    }


    class LocalActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            currentActivity = activity;
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
