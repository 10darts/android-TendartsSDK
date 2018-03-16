package com.tendarts.sdk.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;

import com.tendarts.sdk.BuildConfig;
import com.tendarts.sdk.Model.Notification;
import com.tendarts.sdk.client.TendartsClient;

import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.communications.Communications;
import com.tendarts.sdk.communications.ICommunicationObserver;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by jorgearimany on 4/4/17.
 */

public class PushController
{
	private static final String TAG = "SDK:PUSH";
	private static PushController _me;
	/*
	private static Context _context;*/
	public static final int NOTIFICATION_ID =5525 ;
	public static final int SINGLE_ID= 5526;


	public static int getNotificationId(Notification notification)
	{
		if( ! TendartsSDK.instance().getStackNotifications() && notification != null)
		{
			String code = notification.getCode();
			if (code != null)
			{
				return code.hashCode();
			}
		}
		return SINGLE_ID;
	}
	/*
	public static void clearMessages()
	{
		try
		{
			Log.i(TAG,"clearing messages");
			NotificationManager mNotification = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotification.cancel(NOTIFICATION_ID);
			mNotification.cancel(SINGLE_ID);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}*/
	private PushController()
	{

	}
	public static PushController instance()
	{
		if( _me == null)
		{
			_me = new PushController();
		}
		return _me;

	}


	public static void sendRegistrationToken(final String token, Context context)
	{

		if( token == null )
		{
			return;
		}

		Configuration.instance(context).setPush(token);
		sendTokenAndVersion(token, context);
	}
	public static void sendTokenAndVersion(final String token, Context context)
	{

		sendTokenAndVersion(token,context,false);
	}
	public static void sendTokenAndVersion(final String token, final Context context, final boolean force)
	{


		TendartsSDK.initCommunications(context);

		if( Configuration.getAccessToken(context)== null)
		{

			return;
		}
		int diagnostics = 0;

		String model = "";

		try
		{
			if (Configuration.instance(context).getNotificationsEnabled())
			{
				diagnostics = 1;

			}


			diagnostics+=10*Util.isNotificationEnabled(context);

			if( Util.isGooglePlayServicesAvailable(context))
			{
				diagnostics+=100;
			}
			diagnostics+=1000*  Util.isPrimaryAccountConfigured(context);

			diagnostics += 1000000*Util.getSDKLevel();

			model = Build.MANUFACTURER+"|"+Build.MODEL;


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		String version = "";
		if( context != null)

		{
			try
			{
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				version = pInfo.versionName;
			}
			catch (Exception e )
			{
				Log.e(TAG, e.getMessage());
			}
		}

		final String toSend = token+version;

		String lang = null;
		try
		{
			lang = Locale.getDefault().getLanguage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if( ! toSend.equals( Configuration.instance(context).getPushSentToken()) ||
				force||
				Configuration.instance(context).getPushCode()==null)
		{


			String code = Configuration.instance(context).getPushCode();

			String data = "{ \"token\":\"" + token + "\", \"platform\":\"android\" , \"version\":\"" + version + "\", \"push_status\":"
					+diagnostics+" }";
			try
			{
				JSONObject object = new JSONObject();
				object.put("token", token);
				object.put("platform","android");
				object.put("version",version);
				object.put("push_status", diagnostics );
				object.put("model", model);
				object.put("sdk", BuildConfig.VERSION_NAME);
				object.put("source",Configuration.instance(context).getInstallSource());
				PendingCommunicationController.addPendingTokenInfo(object, context);
				if( lang != null)
				{
					object.put("language", lang);
				}
				Communications.addGeoData(object);
				data = object.toString();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			Log.d(TAG, "sendTokenAndVersion: "+data);
			final String finalData = data;
			final boolean hasCode = code != null;
			ICommunicationObserver observer = new ICommunicationObserver()
			{
				@Override
				public void onSuccess(int operationId, JSONObject data)
				{
					handleSuccess(data);
				}

				public void handleSuccess(JSONObject data)
				{
					try
					{
						Log.d(TAG, "handleSuccess: " + data);
						if (data.has("code") && !data.isNull("code") )
						{
							String code = data.getString("code");
							if (code != null)
							{
								if( code.toLowerCase().contains("null"))
								{
									PendingCommunicationController.addPendingToken("registered with null in code:"+data, context);

								}
								else
								{
									Configuration.instance(context).setPushCode(code);
									Configuration.instance(context).setPushSentToken(toSend);
									PendingCommunicationController.onTokenSent(context);
								}
							}

						}
						else if( ! hasCode)
						{
							PendingCommunicationController.addPendingToken("registered with no code:"+data, context);

						}
						if (data.has("persona") && !data.isNull("persona"))
						{
							String resourceUri = data.getString("persona");
							Configuration.instance(context).setUserCode(resourceUri);
						}

					} catch (Exception e)
					{
						e.printStackTrace();
						PendingCommunicationController.addPendingToken(e.getMessage(), context);

					}
					TendartsClient.instance(context).logEvent("Push", "sent token info", "");
					Log.i(TAG, "succesfully sent token to backend " + token);
				}

				@Override
				public void onFail(int operationId, String reason, Communications.PendingCommunication pc)
				{
					String code = Configuration.instance(context).getPushCode();
					Util.checkUnauthorized(reason, context);
					if (reason != null && reason.contains("400") && code != null)
					{
						Communications.patchData(String.format(Constants.DEVICE, code),
								Util.getProvider(), 0,
								new ICommunicationObserver()
								{
									@Override
									public void onSuccess(int operationId, JSONObject data)
									{
										handleSuccess(data);
									}

									@Override
									public void onFail(int operationId, String reason, Communications.PendingCommunication pc)
									{
										Util.checkUnauthorized(reason, context);
										Log.e(TAG, "error sending token to backend " + token);
										TendartsClient.instance(context).logEvent("Push", "error sending token info", reason);
										PendingCommunicationController.addPendingToken("400:"+reason, context);
									}
								}, finalData, false);
					} else
					{
						if (!force)
						{
							Configuration.instance(context).setPushSentToken(null);
							Configuration.instance(context).setPushUser(null);
						}
						Log.e(TAG, "error sending token to backend " + token);
						TendartsClient.instance(context).logEvent("Push", "error sending token info", reason);
						PendingCommunicationController.addPendingToken(reason, context);
					}

				}
			};
			if( code != null)
			{
				Communications.patchData(String.format(Constants.DEVICE, code), Util.getProvider(),
						0, observer, data,
						false);
			}
			else
			{
				Communications.postData(Constants.DEVICES, Util.getProvider(), 0, observer, data);
			}
		}
	}
}
