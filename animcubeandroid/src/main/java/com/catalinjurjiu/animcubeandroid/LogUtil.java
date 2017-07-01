package com.catalinjurjiu.animcubeandroid;

import android.util.Log;

/**
 * <p>
 * Wrapper methods to several methods from {@link Log}, that only print to logcat if we need them to.
 * </p>
 * Created by catalin on 19.06.2017.
 */
class LogUtil {

    private LogUtil() {
        //private c-tor to prevent instantiation
    }

    static void d(String tag, String message, boolean isDebuggable) {
        if (isDebuggable) {
            Log.d(tag, message);
        }
    }

    static void e(String tag, String message, boolean isDebuggable) {
        if (isDebuggable) {
            Log.e(tag, message);
        }
    }

    static void e(String tag, String message, Throwable t, boolean isDebuggable) {
        if (isDebuggable) {
            Log.e(tag, message, t);
        }
    }

    static void w(String tag, String message, boolean isDebuggable) {
        if (isDebuggable) {
            Log.w(tag, message);
        }
    }
}
