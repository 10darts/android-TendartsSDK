package com.tendarts.sdk.monitoring;


import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tendarts.sdk.client.TendartsClient;
import com.tendarts.sdk.common.Constants;
import com.tendarts.sdk.common.Util;
import com.tendarts.sdk.communications.Communications;
import com.tendarts.sdk.communications.ICommunicationObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by jorgearimany on 13/9/17.
 */

public class IntentMonitorService extends IntentService
{


	private static String TAG = "intent monitor";
	public IntentMonitorService()
	{
		super("default");
	}
	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public IntentMonitorService(String name)
	{
		super(name);
	}

	/**
	 * This method is invoked on the worker thread with a request to process.
	 * Only one Intent is processed at a time, but the processing happens on a
	 * worker thread that runs independently from other application logic.
	 * So, if this code takes a long time, it will hold up other requests to
	 * the same IntentService, but it will not hold up anything else.
	 * When all requests have been handled, the IntentService stops itself,
	 * so you should not call {@link #stopSelf}.
	 *
	 * @param intent The value passed to {@link
	 *               Context#startService(Intent)}.
	 *               This may be null if the service is being restarted after
	 *               its process has gone away; see
	 *               {@link Service#onStartCommand}
	 *               for details.
	 */
	@Override
	protected void onHandleIntent(@Nullable Intent intent)
	{
		String id = "";
		if( intent != null)
		{
			id = intent.getDataString();
		}

		Log.d(TAG, "onHandleIntent: "+id);

		String me = getApplicationContext().getApplicationInfo().packageName;
		long current = new Date().getTime();
		if( !isForeground( me ))
		{
			Log.d(TAG, "not on foreground sleeping ");
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		boolean found = false;
		while ( isForeground(me))
		{
			Log.d(TAG, "on foreground");
			found = true;
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		if( found )
		{
			long elapsed = new Date().getTime() - current;
			final JSONObject object = new JSONObject();
			try
			{
				object.put("push",String.format(Constants.PUSH, id));
				object.put("device",Util.getFullDeviceUrl(getApplicationContext()));
				object.put("kind", Constants.SESSION_EVENT );
				object.put("value", Math.ceil(elapsed/1000.0));
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
			Log.d(TAG, "json: "+object.toString());
			Communications.postData(Constants.EVENTS, Util.getProvider(), 0, new ICommunicationObserver()
			{
				@Override
				public void onSuccess(int operationId, JSONObject data)
				{
					TendartsClient.instance(getApplicationContext()).logEvent("SDK",
							"session sent", ""+data);

					Log.d(TAG, "session sent");
				}

				@Override
				public void onFail(int operationId, String reason, Communications.PendingCommunication pc)
				{
					Util.checkUnauthorized(reason,getApplicationContext());
					TendartsClient.instance(getApplicationContext()).logEvent("SDK",
							"session sent", ""+reason);
					Log.d(TAG, "can't send session" + reason);

				}
			},object.toString());
		}





	}
	public boolean isForeground(String myPackage)
	{
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		return componentInfo.getPackageName().equals(myPackage);
	}
}
