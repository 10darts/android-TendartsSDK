package com.tendarts.sdk.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by jorgearimany on 21/11/16.
 */

public class ConnectionManager
{


	/**
	 * Check if the device is connected to the Internet
	 * @param context context to be used, not null
	 * @return true if there is a connection
	 */
	public static boolean isConnected(Context context)
	{

		NetworkInfo activeNetwork = getNetworkInfo(context);
		Log.d("network", "isConnected: active network "+activeNetwork);
		if( activeNetwork != null)
		{

			boolean isConnected = activeNetwork.isConnected(); //isConnectedOrConnecting();
			return isConnected;
		}

		return false;
	}

	/**
	 * Check if connection is made via wifi
	 * @param context context to be used
	 * @return true if the connection is via wifi
	 */
	public static boolean isConnectedViaWifi( Context context)
	{

		NetworkInfo activeNetwork = getNetworkInfo(context);
		if( activeNetwork != null)
		{
			return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		}

		return false;
	}



	private static NetworkInfo getNetworkInfo(Context context)
	{
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo();
	}
}
