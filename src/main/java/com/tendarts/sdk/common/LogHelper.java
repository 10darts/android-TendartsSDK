package com.tendarts.sdk.common;

import android.content.Context;
import android.util.Log;

import com.tendarts.sdk.BuildConfig;
import com.tendarts.sdk.client.TendartsClient;

public class LogHelper {

    public static final String LOG_CONSOLE_TAG_NET = "NET";
    public static final String LOG_CONSOLE_TAG_10DARTS = "10DARTS";
    public static final String LOG_CONSOLE_TAG_USER = "USER";

    public static final String LOG_EVENT_CATEGORY_10DARTS = "10DARTS";
    public static final String LOG_EVENT_CATEGORY_PUSH = "PUSH";
    public static final String LOG_EVENT_CATEGORY_KEYS = "KEYS";
    public static final String LOG_EVENT_CATEGORY_GEO = "GEO";

    public static void logConsole(String tag, String message) {

        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }

    }
    public static void logConsole(String message) {
        logConsole(LOG_CONSOLE_TAG_10DARTS, message);
    }

    public static void logConsoleNet(String message) {
        logConsole(LOG_CONSOLE_TAG_NET, message);
    }

    public static void logConsoleUser(String message) {
        logConsole(LOG_CONSOLE_TAG_USER, message);
    }

    public static void logEvent(Context context, String category, String type, String message) {
        TendartsClient.instance(context).logEvent(category, type, message);
    }

    public static void logEvent(Context context, String type, String message) {
        TendartsClient.instance(context).logEvent(LOG_EVENT_CATEGORY_10DARTS, type, message);
    }

    public static void logEventPush(Context context, String type, String message) {
        logEvent(context, LOG_EVENT_CATEGORY_PUSH, type, message);
    }

    public static void logEventKeys(Context context, String type, String message) {
        logEvent(context, LOG_EVENT_CATEGORY_KEYS, type, message);
    }

    public static void logEventGeo(Context context, String type, String message) {
        logEvent(context, LOG_EVENT_CATEGORY_GEO, type, message);
    }

}
