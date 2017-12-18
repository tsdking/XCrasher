package com.appgole.crashlib;

import android.annotation.SuppressLint;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by xqf on 2017/12/18.
 */
public class XLooper implements Runnable {
    private static Object EXIT = new Object();
    private static ThreadLocal<XLooper> XLOOPER_THREAD_LOCAL;
    private static Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private static Handler sHandler;

    static {
        XLOOPER_THREAD_LOCAL = new ThreadLocal<>();
        sHandler = new Handler(Looper.getMainLooper());
    }


    public static boolean isSafe() {
        return XLOOPER_THREAD_LOCAL.get() != null;
    }

    public static void start(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        sHandler.removeMessages(0, EXIT);
        sHandler.post(new XLooper());
        XLooper.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public void run() {
        if (isSafe()) {
            return;
        }
        Method next;
        Field target;
        try {
            @SuppressLint("PrivateApi")
            Method method = MessageQueue.class.getDeclaredMethod("next");
            method.setAccessible(true);
            next = method;
            Field targetTmp = Message.class.getDeclaredField("target");
            targetTmp.setAccessible(true);
            target = targetTmp;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        XLOOPER_THREAD_LOCAL.set(this);
        MessageQueue myQueue = Looper.myQueue();
        Binder.clearCallingIdentity();
        while (true) {
            try {
                Message message = (Message) next.invoke(myQueue);
                if (message == null || message.obj == EXIT) {
                    break;
                }
                Handler handler = (Handler) target.get(message);
                handler.dispatchMessage(message);

                Binder.clearCallingIdentity();

                int currentVersion = Build.VERSION.SDK_INT;
                if (currentVersion < Build.VERSION_CODES.LOLLIPOP) {
                    message.recycle();
                }
            } catch (InvocationTargetException exception) {
                Thread.UncaughtExceptionHandler exceptionHandler = uncaughtExceptionHandler;
                Throwable throwable;
                throwable = exception.getCause();
                if (throwable == null) {
                    throwable = exception;
                }
                if (exceptionHandler != null) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), throwable);
                }
                if (sHandler != null) {
                    sHandler.post(this);
                }
                break;
            } catch (Exception e) {
                Thread.UncaughtExceptionHandler exceptionHandler = uncaughtExceptionHandler;
                if (exceptionHandler != null)
                    exceptionHandler.uncaughtException(Thread.currentThread(), e);
                if (sHandler != null) {
                    sHandler.post(this);
                }
                break;
            }
        }
        XLOOPER_THREAD_LOCAL.set(null);
    }
}
