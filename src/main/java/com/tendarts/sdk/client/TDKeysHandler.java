package com.tendarts.sdk.client;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tendarts.sdk.BuildConfig;
import com.tendarts.sdk.common.Configuration;
import com.tendarts.sdk.common.Constants;
import com.tendarts.sdk.common.Util;
import com.tendarts.sdk.communications.Communications;
import com.tendarts.sdk.communications.ICommunicationObserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luisma on 15/3/18.
 */

public class TDKeysHandler {

    private static final String LOG_TAG = TDKeysHandler.class.getSimpleName();

    public interface TDKeysHandlerInterface {
        void onSuccess();
        void onError(String reason);
    }

    public enum KeyValueKind {
        FLOAT(0),
        INT(1),
        STRING(2),
        BOOL(3),
        LIST(4);

        private int kindValue;

        KeyValueKind(int kindValue) {
            this.kindValue = kindValue;
        }

        public int getKindValue() {
            return kindValue;
        }

    }

    public static void associateKeyValueWithDevice(@NonNull final Context context,
                                                   @NonNull final String key,
                                                   @Nullable KeyValueKind kind,
                                                   @Nullable final String value,
                                                   @Nullable final TDKeysHandlerInterface listener) {

        String device = Util.getFullDeviceUrl(context);
        String data = null;

        try {
            JSONObject keyJson = new JSONObject();
            keyJson.put("label", key);

            JSONObject paramsJson = new JSONObject();
            paramsJson.put("key", keyJson);
            if (kind != null) {
                paramsJson.put("kind", kind.getKindValue());
            }
            if (value != null) {
                paramsJson.put("value", value);
            }
            paramsJson.put("device", device);

            data = paramsJson.toString();

        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, e.getLocalizedMessage());
            }
            e.printStackTrace();

            if (listener != null) {
                listener.onError(e.getLocalizedMessage());
            }
        }

        if (data != null) {

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Associate key-value with device: " + data);
            }

            Communications.postData(Constants.KEYS_DEVICES, Util.getProvider(), 0, new ICommunicationObserver() {
                @Override
                public void onSuccess(int operationId, JSONObject data) {

                    String message = "Successfully sent key-value: " + key + "-" + value;

                    TendartsClient.instance(context).logEvent("KEYS", "keyDevice", message);

                    Log.i(LOG_TAG, message);

                    if (listener != null) {
                        listener.onSuccess();
                    }

                }

                @Override
                public void onFail(int operationId, String reason, Communications.PendingCommunication pending) {

                    Util.checkUnauthorized(reason, context);

                    String message = String.format("Error sending key-value: %s-%s. Reason: %s", key, value, reason);

                    TendartsClient.instance(context).logEvent("KEYS", "keyDevice", message);

                    Log.e(LOG_TAG, message);

                    if (listener != null) {
                        listener.onError(reason);
                    }

                }
            }, data);

        }

    }

    public static void associateKeyValueWithUser(@NonNull final Context context,
                                                 @NonNull final String key,
                                                 @Nullable KeyValueKind kind,
                                                 @Nullable final String value,
                                                 @Nullable final TDKeysHandlerInterface listener) {

        String user = Configuration.instance(context).getUserCode();
        if( user == null ) {
            if (listener != null) {
                listener.onError("The user should be already registered");
            }
            return;
        }


        String data = null;

        try {
            JSONObject keyJson = new JSONObject();
            keyJson.put("label", key);

            JSONObject paramsJson = new JSONObject();
            paramsJson.put("key", keyJson);
            if (kind != null) {
                paramsJson.put("kind", kind.getKindValue());
            }
            if (value != null) {
                paramsJson.put("value", value);
            }
            paramsJson.put("persona", user);

            data = paramsJson.toString();

        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, e.getLocalizedMessage());
            }
            e.printStackTrace();

            if (listener != null) {
                listener.onError(e.getLocalizedMessage());
            }
        }

        if (data != null) {

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Associate key-value with user: " + data);
            }

            Communications.postData(Constants.KEYS_PERSONAS, Util.getProvider(), 0, new ICommunicationObserver() {
                @Override
                public void onSuccess(int operationId, JSONObject data) {

                    String message = "Successfully sent key-value: " + key + "-" + value;

                    TendartsClient.instance(context).logEvent("KEYS", "keyUser", message);

                    Log.i(LOG_TAG, message);

                    if (listener != null) {
                        listener.onSuccess();
                    }

                }

                @Override
                public void onFail(int operationId, String reason, Communications.PendingCommunication pending) {

                    Util.checkUnauthorized(reason, context);

                    String message = String.format("Error sending key-value: %s-%s. Reason: %s", key, value, reason);

                    TendartsClient.instance(context).logEvent("KEYS", "keyUser", message);

                    Log.e(LOG_TAG, message);

                    if (listener != null) {
                        listener.onError(reason);
                    }

                }
            }, data);

        }

    }

}
