package com.tendarts.sdk.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.tendarts.sdk.communications.Communications;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.tendarts.sdk.client.TendartsClient;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by jorgearimany on 4/4/17.
 */

public class Util
{

	private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
	private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

	@SuppressLint("NewApi")
	public static int isNotificationEnabled( Context context)
	{
		try
		{
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

				ApplicationInfo appInfo = context.getApplicationInfo();

				String pkg = context.getApplicationContext().getPackageName();

				int uid = appInfo.uid;

				Class appOpsClass = null; /* Context.APP_OPS_MANAGER */


				appOpsClass = Class.forName(AppOpsManager.class.getName());

				Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

				Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
				int value = (int) opPostNotificationValue.get(Integer.class);

				return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED) ? 1 : 0;
			}

		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return 2;
	}
	public static boolean isGooglePlayServicesAvailable(Context context){
		GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
		return resultCode == ConnectionResult.SUCCESS;
	}
	//1-- yes, 0--no 2--error
	public static int isPrimaryAccountConfigured(Context context)
	{
		try
		{
			//Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
			Account[] accounts = AccountManager.get(context).getAccounts();
			if(accounts.length > 0)
			{
				return 1;
			}
			return 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 2;

	}

	public static int getSDKLevel()
	{
		try
		{

			return Build.VERSION.SDK_INT;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	public static void printExtras(String tag, Bundle extras)
	{
		try
		{
			StringBuilder builder = new StringBuilder("Extras:\n");
			if( extras == null)
			{
				builder.append("null");
			}
			else
			{
				for (String key : extras.keySet())
				{ //extras is the Bundle containing info
					Object value = extras.get(key); //get the current object
					builder.append(key).append(": ").append(value).append("\n"); //add the key-value pair to the
				}
			}
			Log.i(tag, builder.toString()); //log the data or use it as needed.
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}



	public static  void setBadgeCount( int count, Context context)
	{
		try
		{
			Configuration.instance(context).setLastBadge(count);
			Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
			intent.putExtra("badge_count", count);
			intent.putExtra("badge_count_package_name", context.getPackageName());
			intent.putExtra("badge_count_class_name", TendartsClient.instance(context).mainActivityClassName());
			context.sendBroadcast(intent);


		}
		catch( Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			Intent intent = new Intent("com.sonyericsson.home.action.UPDATE_BADGE");
			intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", TendartsClient.instance(context).mainActivityClassName());
			intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", true);
			intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));
			intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());
			context.sendBroadcast(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getFullDeviceUrl (Context context )
	{
		return String.format( Constants.device,Configuration.instance(context).getPushCode());

	}

	public static String getDeviceJson(Context context)
	{
		String deviceId = String.format( Constants.deviceReference,Configuration.instance(context).getPushCode());
		String json = null;
		try
		{
			JSONObject object = new JSONObject();
			object.put("device",deviceId );
			json = object.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Log.d("NET:", "DeviceJson: "+json);
		return json;
	}




	/**
	 *   getResId("icon", Drawable.class);
	 * @param resName
	 * @param c
	 * @return
	 */
	public static int getResId(String resName, Class<?> c) {

		try {
			Field idField = c.getDeclaredField(resName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	static Communications.ICommunicationsConfigProvider _provider = null;
	public static Communications.ICommunicationsConfigProvider getProvider()
	{
		if( _provider == null)
		{
			final Context context = Communications.getContext();
			_provider =
			new Communications.ICommunicationsConfigProvider()
			{
				@Override
				public String getPushCode()
				{
					return Configuration.instance(context).getPushCode();
				}

				@Override
				public String getGeostatsUrlFormat()
				{
					return Constants.geostats;
				}

				@Override
				public ArrayList<Communications.CHeader> getHeaders()
				{
					Communications.CHeader header = new Communications.CHeader("Authorization", "Token " +
							Configuration.instance(context).getAccessToken(context));
					ArrayList<Communications.CHeader> list = new ArrayList<Communications.CHeader>();
					list.add(header);


					return list;
				}

				@Override
				public void onGeostatSent(boolean success, String info)
				{
					if (success)
					{
						TendartsClient.instance(context).logEvent("GEO", "Succesfully  sent geoStats", "");
					} else
					{
						TendartsClient.instance(context).logEvent("GEO", "Failed to send geoStats", "" + info);
					}
				}
			};
		}
		return _provider;
	}

	public static void checkUnauthorized(String reason, Context context)
	{
		if( reason != null && reason.contains("401"))
		{
			TendartsClient.instance(context).remoteLogException(new Exception(reason));
		}
	}
}
