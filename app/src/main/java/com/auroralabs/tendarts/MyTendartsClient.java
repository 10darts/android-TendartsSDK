package com.auroralabs.tendarts;

import android.content.Context;
import android.util.Log;

import com.tendarts.sdk.client.TendartsClient;

/**
 * Created by luisma on 6/2/18.
 */

public class MyTendartsClient extends TendartsClient {

    private static final String LOG_TAG = MyTendartsClient.class.getSimpleName();

    @Override
    public String mainActivityClassName() {
        return MainActivity.class.getName();
    }

    @Override
    public CharSequence getLocationExplanation(Context context) {
        // Returns the explanation of why your app uses location and why permission should be granted
        return "We need permissions to access your location so we can send you notifications depending on where you are";
    }

//    @Override
//    public void performSetup(Context context) {
//
//        // The SDK configuration is done in the performSetup() function, just add your custom configuration here
//        TendartsSDK.instance().stackNotifications(true)
//                .alwaysShowLastNotification(true)
//                .limitNotificationSoundAndVibrationTime(true)
//                .setLargeIconResource(R.mipmap.ic_launcher);
//
//    }


    @Override
    public void onUserAcceptedLocationPermission() {

        // Called if 'linkDeviceWithUserIdentifier' fails and then the retry works

    }

    @Override
    public void logEvent(String category, String type, String message) {

        if (BuildConfig.DEBUG) {

            Log.d(LOG_TAG, "category: '" + category + "', type: '" + type + "', message: '" + message + "'");

        }

    }
}
