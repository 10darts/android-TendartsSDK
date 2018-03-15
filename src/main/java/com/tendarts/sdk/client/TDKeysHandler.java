package com.tendarts.sdk.client;

import android.content.Context;
import android.util.Log;

import com.tendarts.sdk.BuildConfig;
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

    public void associateKeyValueWithDevice(final Context context,
                                            final String key, KeyValueKind kind, final String value,
                                            final TDKeysHandlerInterface listener) {

        if (key == null || value == null) {
            TendartsClient.instance(context).logEvent("KEYS", "keys send error", "Set keys, a type and a value");
        }

        String device = Util.getFullDeviceUrl(context);
        String data = null;

        try {
            JSONObject keyJson = new JSONObject();
            keyJson.put("label", key);

            JSONObject paramsJson = new JSONObject();
            paramsJson.put("key", keyJson);
            paramsJson.put("kind", kind.getKindValue());
            paramsJson.put("value", value);
            paramsJson.put("device", device);

            data = paramsJson.toString();

        } catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, e.getLocalizedMessage());
            }
            e.printStackTrace();
        }

        if (data != null) {

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Associate key-value with device: " + data);
            }

            ICommunicationObserver observer = new ICommunicationObserver() {
                @Override
                public void onSuccess(int operationId, JSONObject data)
                {
                    handleSuccess(data);
                }

                private void handleSuccess(JSONObject data) {

                    String message = "Successfully sent key-value " + key + "-" + value;

                    TendartsClient.instance(context).logEvent("KEYS", "keyDevice", message);

                    Log.i(LOG_TAG, message);

                    if (listener != null) {
                        listener.onSuccess();
                    }

                }

                @Override
                public void onFail(int operationId, String reason, Communications.PendingCommunication pc) {

                    Util.checkUnauthorized(reason, context);

                    String message = String.format("Error sending key-value: %s-%s. Reason: %s", key, value, reason);

                    TendartsClient.instance(context).logEvent("KEYS", "keyDevice", message);

                    Log.e(LOG_TAG, message);

                    if (listener != null) {
                        listener.onError(reason);
                    }

                }
            };

            Communications.postData(Constants.devices, Util.getProvider(), 0, observer, data);

        }

    }

}
