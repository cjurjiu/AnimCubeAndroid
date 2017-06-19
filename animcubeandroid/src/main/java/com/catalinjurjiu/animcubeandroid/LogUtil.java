package com.catalinjurjiu.animcubeandroid;

import android.util.Log;

/**
 * Created by catalin on 19.06.2017.
 */

public class LogUtil {

    public static void d(String tag, String message, boolean isDebug) {
        if (isDebug) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message, boolean isDebug) {
        if (isDebug) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable t, boolean isDebug) {
        if (isDebug) {
            Log.e(tag, message, t);
        }
    }
}
