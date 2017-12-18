package com.appgole.xcrasher;

import android.app.Application;

import com.appgole.crashlib.XCrasher;

/**
 * Created by xqf on 2017/12/18.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XCrasher.init(this);
    }
}
