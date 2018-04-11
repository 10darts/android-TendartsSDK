package com.tendarts.sdk.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.tendarts.sdk.common.LogHelper;

/**
 * Created by jorgearimany on 3/4/17.
 */

public class GCMInstanceIdListener extends InstanceIDListenerService {

	private static final String TAG = "SDK GCM ID listener";

	@Override
	public void onTokenRefresh() {
		LogHelper.logConsole(TAG, "onTokenRefresh:");
		super.onTokenRefresh();
		Intent intent = new Intent(this, GCMRegistrationIntentService.class);
		startService(intent);
	}

}
