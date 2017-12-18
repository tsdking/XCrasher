package com.appgole.crashlib;

import android.app.Activity;

/**
 * Created by xqf on 2017/12/18.
 */
public interface CrashListener {
    void onCrash(Throwable throwable, Activity activity);
}
