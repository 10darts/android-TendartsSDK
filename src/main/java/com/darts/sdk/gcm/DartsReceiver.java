package com.darts.sdk.gcm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


import com.darts.sdk.Model.PersistentPush;
import com.darts.sdk.Model.Notification;
import com.darts.sdk.client.DartsClient;
import com.darts.sdk.common.Configuration;
import com.darts.sdk.common.Constants;
import com.darts.sdk.common.Util;
import com.darts.sdk.communications.Communications;
import com.darts.sdk.communications.ICommunicationObserver;


import org.json.JSONObject;

/**
 * Created by jorgearimany on 19/4/17.
 */

public class DartsReceiver extends BroadcastReceiver
{


	private static final String TAG = "DartsReceiver";

	static Thread thread;

	@Override
	public void onReceive(final Context context, final Intent intent)
	{


		if(intent!=null){


			String action = intent.getAction();

			Bundle extras = intent.getExtras();
			if( extras == null|| !extras.containsKey("sorg") )
			{
				Log.e(TAG, "onReceive: no extras");
				return;
			}

			int origin = extras.getInt("sorg");
			if( origin != Configuration.instance(context).getAccessToken(context).hashCode() )
			{
				Log.d(TAG, "onReceive:  not for me" );
				return;
			}

			if( "com.darts.sdk.CLEAR_PUSHES".equalsIgnoreCase(action))
			{

				PersistentPush.clear(context);
				DartsClient.instance(context).onNotificationListCleared();
				dismissNotificationIfNeeded(context, intent);

				try
				{
					DartsClient.instance(context).logEvent("Push","clear list from push","");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				return;
			}
			else if ("com.darts.sdk.OPEN_PUSH".equalsIgnoreCase(action))
			{
				//open push
				if( Notification.canDeserialize(intent))
				{

					thread = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							Notification push  = new Notification(intent);

							String json = Util.getDeviceJson(context);

							String url = String.format(Constants.pushClicked, push.getCode());
							Log.d("NET", "sending pchd: "+url+"\n"+json);
							Communications.patchData(url,
									Util.getProvider(), 0, new ICommunicationObserver()
									{
										@Override
										public void onSuccess(int operationId, JSONObject data)
										{
											DartsClient.instance(context).logEvent("Push","succesfully notified follow","");

											Log.d("DARTS", "push read notified");
										}

										@Override
										public void onFail(int operationId, String reason)
										{
											Util.checkUnauthorized(reason,context);
											DartsClient.instance(context).logEvent("App","can't notify follow",""+reason);
											Log.d("DARTS", "push read failed: "+reason);
										}
									}, json,false);

							dismissNotificationIfNeeded(context,intent);
							DartsClient.instance(context).onNotificationClicked(push);
							boolean opened =  DartsClient.instance(context).openNotification(push, context);
							if( ! opened)
							{
								String deepUrl = push.getDeepUrl();
								if( deepUrl != null )
								{
									try
									{
										Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepUrl.trim()));
										viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										context.startActivity(viewIntent);
									}
									catch (Exception e)
									{
										e.printStackTrace();
										DartsClient.instance(context).logEvent("App","can't launch deep url",""+e.getMessage());
										DartsClient.instance(context).remoteLogException(e);
									}
								}
							}
						}
					});
					thread.start();


				}
			}
			else if("com.darts.sdk.OPEN_LIST".equalsIgnoreCase(action))
			{
				dismissNotificationIfNeeded(context, intent);

				DartsClient.instance(context).openNotificationList(context);
			}



		}
	}

	private void dismissNotificationIfNeeded(Context context, Intent intent)
	{
		if( intent.hasExtra("dismiss"))
		{
			int id = intent.getIntExtra("dismiss", -1);
			if( id != -1)
			{
				Log.d(TAG, "dismissNotificationIfNeeded: "+id);

				NotificationManager manager =
						(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(id);
				//manager.cancel(GCMListenerService.not_id);

			}
		}
	}


}
