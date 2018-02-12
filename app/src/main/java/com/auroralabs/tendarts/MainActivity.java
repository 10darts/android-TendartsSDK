package com.auroralabs.tendarts;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.common.Configuration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        configureView();

        // Set up SDK
        TendartsSDK.onCreate(savedInstanceState, this, new TendartsSDK.ILocationAlerter() {
            @Override
            public void alertNotEnabled(Activity activity) {

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Location");
                alertDialog.setMessage("Location not enabled");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        });

        // To get notified when a new location is available, you should register your listener
        TendartsSDK.registerGeoLocationReceiver(new TendartsSDK.IGeoLocationReceiver() {
            @Override
            public void onNewLocation(TendartsSDK.GeoLocation geoLocation) {
                // New location available
            }
        });

        // Enable geolocation updates (enabled by default)
        //TendartsSDK.enableGeolocationUpdates();

        // Disable geolocation updates:
        //TendartsSDK.disableGeolocationUpdates();

        //linkDeviceWithUserIdentifier();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Needed for Geolocation
        TendartsSDK.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up SDK
        TendartsSDK.onResume(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Needed for Geolocation
        TendartsSDK.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Needed for Geolocation
        TendartsSDK.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Needed for Geolocation
        TendartsSDK.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Needed to pass location permissions request result to SDK
        TendartsSDK.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults,
                getApplicationContext());

    }

    private void configureView() {

        TextView accessTokenText = findViewById(R.id.access_token_text);
        TextView senderIdText = findViewById(R.id.sender_id_text);
        TextView modelText = findViewById(R.id.model_text);
        TextView androidVersionText = findViewById(R.id.android_version_text);

        String accessToken = Configuration.getAccessToken(this);
        if (!TextUtils.isEmpty(accessToken)) {
            accessTokenText.setText(String.format("10darts SDK Access Token: %s", accessToken));
        }

        String senderId = Configuration.instance(this).getGCMDefaultSenderId(this, getApplicationInfo().packageName);
        if (!TextUtils.isEmpty(senderId)) {
            senderIdText.setText(String.format("GCM SenderId: %s", senderId));
        }

        String model = Build.MANUFACTURER+" | "+Build.MODEL;
        modelText.setText(String.format("Model: %s", model));

        String androidVersion = currentAndroidVersion();
        androidVersionText.setText(String.format("Android version: %s", androidVersion));

    }

    /**
     * Associates the device with your own user
     *
     * As the doc says:
     * 'From version 1.22, if this function fails, it will be automatically retried and when
     * succesfull your client class’s onUserLinkedToDevice function will be called'
     */
    private void linkDeviceWithUserIdentifier() {

        TendartsSDK.linkDeviceWithUserIdentifier(new TendartsSDK.IResponseObserver() {
            @Override
            public void onOk() {
                // Device linked, save it to not re-link again
            }

            @Override
            public void onFail(String errorString) {
                // Something failed, try again later, more info on errorString
            }
        },this,"my-user-identifier");

    }

    /**
     * Optionally, you can add user data,
     * to do so, call modifyUser providing the fields you want to add or modify,
     * if any parameter is null it will remain untouched.
     */
    private void addUserData() {

        TendartsSDK.modifyUser("email", "first name", "last name", "password", new TendartsSDK.IResponseObserver() {
            @Override
            public void onOk() {
                // succeeded
            }

            @Override
            public void onFail(String errorString) {
                // failed
            }
        }, this);

    }

    // Current Android version data
    public static String currentAndroidVersion(){
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)","$1"));
        String codeName = "Unsupported";//below Jelly bean OR above Oreo
        if(release >= 4.1 && release < 4.4) {
            codeName="Jelly Bean";
        } else if (release<5) {
            codeName="Kit Kat";
        } else if (release<6) {
            codeName="Lollipop";
        } else if (release<7) {
            codeName="Marshmallow";
        } else if (release<8) {
            codeName="Nougat";
        } else if (release<9) {
            codeName="Oreo";
        }
        return codeName+" v"+release+", API Level: "+Build.VERSION.SDK_INT;
    }

}
