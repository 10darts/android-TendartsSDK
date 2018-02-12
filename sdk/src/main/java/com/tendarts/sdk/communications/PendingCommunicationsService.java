package com.tendarts.sdk.communications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.common.ConnectionManager;
import com.tendarts.sdk.common.PendingCommunicationController;

import java.util.Date;

/**
 * Created by jorgearimany on 10/10/17.
 */

public class PendingCommunicationsService extends IntentService
{

	public static final String TAG = "PendingCommunicationsS";
	private static String CONTINUE_PENDING_COMMUNICATIONS = "com.com.auroralabs.tendarts.sdk.CONTINUE_PENDING_COMMUNICATIONS";
	public PendingCommunicationsService()
	{
		super("PendingCommunicationsService");
	}
	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public PendingCommunicationsService(String name)
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
		Log.d(TAG, "onHandleIntent: ");
		PendingCommunicationController.doPending(getApplicationContext());

	}
	/**
	 * This is called if the service is currently running and the user has
	 * removed a task that comes from the service's application.  If you have
	 * set {@link ServiceInfo#FLAG_STOP_WITH_TASK ServiceInfo.FLAG_STOP_WITH_TASK}
	 * then you will not receive this callback; instead, the service will simply
	 * be stopped.
	 *
	 * @param rootIntent The original root Intent that was used to launch
	 *                   the task that is being removed.
	 */
	@Override
	public void onTaskRemoved(Intent rootIntent)
	{
		Log.d(TAG, "onTaskRemoved: scheduling");
		schedulePending(60000,getApplicationContext());


		super.onTaskRemoved(rootIntent);
	}

	public static void schedulePending(long milliseconds, Context context)
	{
		Intent restartServiceIntent = new Intent(context, PendingCommunicationsService.class);
		restartServiceIntent.setPackage(context.getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(context, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmService.set(
				AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + milliseconds,
				restartServicePendingIntent);

		Log.d(TAG, "scheluded "+milliseconds/1000f);
	}


	public static void startPendingCommunications(Context context)
	{

		Log.d(TAG, "startPendingCommunications: ");
		Communications.init(context);

		//Communications.init(context.getApplicationContext());
		Intent intent = new Intent(context, PendingCommunicationsService.class);
		intent.setAction(CONTINUE_PENDING_COMMUNICATIONS);
		context.startService(intent);
	}


}
