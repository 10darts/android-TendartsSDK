package com.tendarts.sdk.communications;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;

import com.tendarts.sdk.common.ConnectionManager;
import com.tendarts.sdk.common.LogHelper;

public class NetworkChangeReceiver extends BroadcastReceiver
{
	private static final String TAG = "NetworkChangeReceiver";
	private static NetworkChangeReceiver lastReceiver = null;
	public static void enable(Context context)
	{
		ComponentName receiver = new ComponentName(context,NetworkChangeReceiver.class);
		PackageManager pm = context.getPackageManager();
		if( pm != null	)
		{

			pm.setComponentEnabledSetting(receiver,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
			try
			{
				lastReceiver = new NetworkChangeReceiver();
				IntentFilter filter = new IntentFilter();
				filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
				context.getApplicationContext().registerReceiver(lastReceiver, filter);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.e(TAG,"could not enable package manager");
		}
	}

	public static void disable( Context context)
	{
		ComponentName receiver = new ComponentName(context,NetworkChangeReceiver.class);
		PackageManager pm = context.getPackageManager();
		if( pm != null	)
		{
			pm.setComponentEnabledSetting(receiver,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
		}
		else
		{
			Log.e(TAG,"could not disable package manager");
		}
		try
		{
			context.getApplicationContext().unregisterReceiver(lastReceiver);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public NetworkChangeReceiver()
	{
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		LogHelper.logConsole(TAG, "Network change received");
		if (ConnectionManager.isConnected(context)) {
			//if connected start pending updates and disable receiver
			PendingCommunicationsService.startPendingCommunications(context);
			disable(context);
		}
	}
}
