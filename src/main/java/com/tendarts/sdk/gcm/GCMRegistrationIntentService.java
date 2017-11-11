package com.tendarts.sdk.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tendarts.sdk.common.Util;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tendarts.sdk.client.TendartsClient;
import com.tendarts.sdk.common.Configuration;
import com.tendarts.sdk.common.PushController;

import java.io.IOException;

/**
 * Created by jorgearimany on 3/4/17.
 */

public class GCMRegistrationIntentService extends IntentService
{

	private static final String[] TOPICS = {"global"};
	private static final String TAG = "GCM Intent Service";
	public static final String PUSH_CATEGORY = "Push";

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 *  name Used to name the worker thread, important only for debugging.
	 */
	public GCMRegistrationIntentService()
	{
		super("RegistrationIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{

		try
		{
			Log.d(TAG, "onHandleIntent: ");

			String senderId = Configuration.instance(getApplicationContext())
					.getGCMDefaultSenderId(getApplicationContext(),getApplicationInfo().packageName);

			//String sender2 = Configuration.instance(getApplicationContext()).getGCMDefaultSenderId(getApplicationContext());

			Log.d(TAG, "Sender: "+senderId);

			Util.printExtras(TAG,intent.getExtras());

			if(senderId == null)
			{
				return;
			}

			// [START register_for_gcm]
			// Initially this call goes out to the network to retrieve the token, subsequent calls
			// are local.
			// R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
			// [START get_token]
			InstanceID instanceID = InstanceID.getInstance(this);
			String token = instanceID.getToken(senderId, //Configuration.instance(getApplicationContext()).getGCMDefaultSenderId(getApplicationContext()),
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			// [END get_token]
			Log.i(TAG, "GCM Registration Token: " + token);

			sendRegistrationToServer(token, getApplicationContext());

			// Subscribe to topic channels
			subscribeTopics(token);

			// [END register_for_gcm]
		}
		catch (Exception e)
		{
			Log.d(TAG, "Failed to complete token refresh exception:", e);
			// If an exception happens while fetching the new token or updating our registration data
			// on a third-party server, this ensures that we'll attempt the update at a later time.
			e.printStackTrace();
			Configuration.instance(getApplicationContext()).setPushSentToken(null);

			try
			{
				TendartsClient.instance(getApplicationContext()).remoteLogException(e);
				TendartsClient.instance(getApplicationContext()).logEvent(PUSH_CATEGORY,"errorRegister", e.getMessage());
			} catch (Exception es)
			{
				es.printStackTrace();
			}
		}


	}

	private void sendRegistrationToServer(String reg_id, Context context)
	{

		if( reg_id != null && reg_id.length() > 1 )
		{
			//handle new registration id:

			String saved_id = Configuration.instance(getApplicationContext()).getPush();
			if( !reg_id.equalsIgnoreCase(saved_id) || Configuration.instance(context).getPushSentToken() == null
					|| Configuration.instance(context).getPushCode() == null)
			{
				PushController.sendRegistrationToken(reg_id, getApplicationContext());
				Log.i(TAG, "new reg id");
			}
			else
			{
				Log.i(TAG, "same reg id");
			}


		}

		try
		{
			TendartsClient.instance(getApplicationContext()).logEvent(PUSH_CATEGORY,"registered", "id:" + reg_id.hashCode());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
	 *
	 * @param token GCM token
	 * @throws IOException if unable to reach the GCM PubSub service
	 */
	// [START subscribe_topics]
	private void subscribeTopics(String token) throws IOException
	{
		GcmPubSub pubSub = GcmPubSub.getInstance(this);
		for (String topic : TOPICS) {
			pubSub.subscribe(token, "/topics/" + topic, null);
		}
	}
	// [END subscribe_topics]
}
