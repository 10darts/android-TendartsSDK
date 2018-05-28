package com.tendarts.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.tendarts.sdk.Model.Notification;
import com.tendarts.sdk.Model.PersistentPush;
import com.tendarts.sdk.client.TDKeysHandler;
import com.tendarts.sdk.client.TendartsClient;
import com.tendarts.sdk.common.Configuration;
import com.tendarts.sdk.common.Constants;
import com.tendarts.sdk.common.LogHelper;
import com.tendarts.sdk.common.PendingCommunicationController;
import com.tendarts.sdk.common.PushController;
import com.tendarts.sdk.common.Util;
import com.tendarts.sdk.communications.Communications;
import com.tendarts.sdk.communications.ICommunicationObserver;
import com.tendarts.sdk.communications.PendingCommunicationsService;
import com.tendarts.sdk.gcm.DartsReceiver;
import com.tendarts.sdk.gcm.GCMListenerService;
import com.tendarts.sdk.gcm.GCMRegistrationIntentService;
import com.tendarts.sdk.geo.GoogleUpdates;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * DartsSDK class enables customization of the SDK
 */

public class TendartsSDK {

	static TendartsSDK _me;

	boolean _stackNotifications = false;
	boolean _alwaysShowLastNotification = true;
	boolean _limitNotificationSoundAndVibrationTime = true;
	boolean _onlySmallIcon = false;
	int _notificationSoundAndVibrationFirstHour = 8;
	int _notificationSoundAndVibrationLastHour = 23;//>=start < last
	int _notificationColorResource = R.color.notification_color;
	private int _smallIconResource= android.R.drawable.ic_popup_reminder;// @android:drawable/ic_popup_reminder//R.drawable.ic_not_small_icon;
	private int _largeIconResource= 0;//R.mipmap.ic_not_large_icon;//R.mipmap.ic_launcher
	private String _stackedNotificationContent;
	private CharSequence _stackedNotificationTitle;
	private int viewStackedIconResource = android.R.drawable.ic_menu_view;
	private CharSequence viewStackedString;
	private int cancelStackedIconResource = android.R.drawable.ic_menu_close_clear_cancel;
	private CharSequence cancelStackedString;


	public static TendartsSDK instance()
	{
		if( _me == null)
		{
			_me = new TendartsSDK();
		}
		return _me;
	}

	private TendartsSDK()
	{

	}
	/**
	 * Configure to show stacked notifications or not
	 * defaults to false
	 * @param stackNotifications if true notifications are stacked
	 *
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK stackNotifications(boolean stackNotifications)
	{
		_stackNotifications = stackNotifications;
		return this;
	}

	public boolean getStackNotifications()
	{
		return _stackNotifications;
	}
	//---------------------------------------
	/**
	 * Configure to show always the last notification, useful in combination with stackNotifications
	 * defaults to true
	 * @param alwaysShowLastNotification If the last notification should allways been shown
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK alwaysShowLastNotification(boolean alwaysShowLastNotification)
	{
		_alwaysShowLastNotification = alwaysShowLastNotification;
		return this;
	}

	public boolean getAlwaysSowLastNotification()
	{
		return _alwaysShowLastNotification;
	}


	//---------------------------------------

	/**
	 * Configure to limit when notification will make sound and vibration
	 * defaults to true
	 * if enabled notifications will only make sound between [firstHour ... lastHour)
	 * @param limitNotificationSoundAndVibrationTime If the sound and vibration will be limited
	 *
	 * @return SDK instance so you can concatenate calls
	 *  @see #notificationSoundAndVibrationFirstHour(int)
	 *  @see #notificationSoundAndVibrationLastHour(int)
	 *
	 */
	public TendartsSDK limitNotificationSoundAndVibrationTime(
			boolean limitNotificationSoundAndVibrationTime)
	{
		_limitNotificationSoundAndVibrationTime = limitNotificationSoundAndVibrationTime;
		return this;
	}

	public boolean getLimitNotificationSoundAndVibrationTime()
	{
		return _limitNotificationSoundAndVibrationTime;
	}
	//---------------------------------------
	/**
	 * Configure to set the first hour that notifications will make sound and vibration
	 * Will only taken into account if {@link #limitNotificationSoundAndVibrationTime(boolean)} is set to true
	 * @return SDK instance so you can concatenate calls
	 * @param firstHour first hour when notifications will make sound and vibration. [0..23]
	 * @see #limitNotificationSoundAndVibrationTime(boolean)
	 */
	public TendartsSDK notificationSoundAndVibrationFirstHour(int firstHour)
	{
		_notificationSoundAndVibrationFirstHour = firstHour;
		return this;
	}
	public  int getNotificationSoundAndVibrationFirstHour()
	{
		return _notificationSoundAndVibrationFirstHour;
	}

	//---------------------------------------

	/**
	 * Configure to set the first hour that notifications will make sound and vibration
	 * Will only taken into account if {@link #limitNotificationSoundAndVibrationTime(boolean)} is set to true
	 * @param lastHour last hour when notifications make sound and vibration. [0..23]
	 * @return SDK instance so you can concatenate calls
	 * @see #limitNotificationSoundAndVibrationTime(boolean)
	 */
	public TendartsSDK notificationSoundAndVibrationLastHour(int lastHour)
	{
		_notificationSoundAndVibrationLastHour = lastHour;
		return this;
	}

	public int getNotificationSoundAndVibrationLastHour()
	{
		return _notificationSoundAndVibrationLastHour;
	}


	//---------------------------------------

	/**
	 * Set the color resource to use in Notifications
	 * @param colorResource the color to be used
	 *
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK notificationColorResource(int colorResource)
	{
		_notificationColorResource = colorResource;
		return  this;
	}



	public int getNotificationColorResource()
	{
		return _notificationColorResource;
	}

	//---------------------------------------


	/**
	 * Set the small icon resource
	 * note: if not set, the small icon defaults to android's ic_popup_reminder
	 * @param smallIconResource small icon resource
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK setSmallIconResource(int smallIconResource)
	{
		_smallIconResource = smallIconResource;
		return this;
	}

	public int getSmallIconResource()
	{
		return _smallIconResource;
	}


	//---------------------------------------

	/**
	 * Set that notifications only display small icon
	 * @param onlySmallIcon
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK setOnlySmallIcon(boolean onlySmallIcon)
	{
		_onlySmallIcon = onlySmallIcon;
		return this;
	}
	//---------------------------------------


	/**
	 * Set the large icon resource, if not set your app icon is used
	 * @param largeIconResource large icon resource
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK setLargeIconResource(int largeIconResource)
	{
		_largeIconResource = largeIconResource;
		return this;
	}

	public int getLargeIconResource()
	{
		return _largeIconResource;
	}


	public Bitmap getLargeIcon(Context context)
	{
		try
		{

			if( _onlySmallIcon)
			{
				return null;
			}

			if (context == null || _largeIconResource != 0)
			{
				return BitmapFactory.decodeResource(context.getResources(), _largeIconResource);

			}

			//context.getApplicationInfo().icon
			int id = context.getApplicationInfo().icon;
			if (id != 0)
			{
				return BitmapFactory.decodeResource(context.getResources(), id);
			}

			Drawable drawable = context.getPackageManager().getApplicationIcon(context.getApplicationInfo());
			if (drawable != null)
			{

				Bitmap bitmap = null;

				if (drawable instanceof BitmapDrawable)
				{
					BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
					if (bitmapDrawable.getBitmap() != null && !bitmapDrawable.getBitmap().isRecycled())
					{
						return bitmapDrawable.getBitmap();
					}
				}

				if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
				{
					bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
				} else
				{
					bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				}

				Canvas canvas = new Canvas(bitmap);
				drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
				drawable.draw(canvas);
				return bitmap;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		///ultra fallback, should not happen
		return BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_not_large_icon);
	}


	/**
	 * set the stacked notification Title
	 * @param title the title to be used
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK stackedNotificationTitle(CharSequence title)
	{
		_stackedNotificationTitle = title;
		return this;
	}

	public CharSequence getStackedNotificationTitle()
	{
		return _stackedNotificationTitle;
	}


	/**
	 * set the stacked notifications content
	 * @param content content, if you have an '%d' in the string it will be formatted with
	 *                   the number of pending notifications
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK stackedNotificationContent(String content)
	{
		_stackedNotificationContent = content;
		return  this;

	}

	public String getStackedNotificationContent( int size)
	{
		if( _stackedNotificationContent != null)
		{
			if( _stackedNotificationContent.contains("%d"))
			{
				return String.format(_stackedNotificationContent, size);
			}
			else
			{
				return _stackedNotificationContent;
			}
		}
		return _stackedNotificationContent;
	}


	/**
	 * set the view icon resource in stacked notification
	 * @param iconResource the icon resource, if not set defaults to android.R.drawable.ic_menu_view
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK viewStackedIconResource(int iconResource)
	{
		viewStackedIconResource = iconResource;
		return this;
	}

	public int getViewStackedIconResource()
	{
		return viewStackedIconResource;
	}

	/**
	 * set the view string in stacked notification
	 * @param string
	 * @return  SDK instance so you can concatenate calls
	 */
	public TendartsSDK viewStackdString(CharSequence string)
	{
		viewStackedString = string;
		return this;
	}
	public  CharSequence getViewStackedString()
	{
		return viewStackedString;
	}


	/**
	 * set the cancel button icon in stacked notification
	 * @param iconResource the icon resource to be used, if not set defaults to android.R.drawable.ic_menu_close_clear_cancel
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK cancelStackedIconResource(int iconResource)
	{
		cancelStackedIconResource = iconResource;
		return this;
	}


	/**
	 * android.R.drawable.ic_menu_close_clear_cancel
	 * @return
	 */
	public  int getCancelStackedIconResource()
	{
		return cancelStackedIconResource;
	}

	/**
	 * set the cancel button string in stacked notification
	 * @param string
	 * @return SDK instance so you can concatenate calls
	 */
	public TendartsSDK cancelStackedString (CharSequence string)
	{
		cancelStackedString = string;
		return this;
	}
	public CharSequence getCancelStackedString()
	{
		return cancelStackedString;
	}

/*
	void setClient()
	{

	}
*/

	//------------------------------------------------------------------
	//			MAIN ACTIVITY NOTIFICICATIONS
	//------------------------------------------------------------------
	public static final int REQUEST_LOCATION = 333;

	private static Date lastHeartbeat = new Date(0);

	static GoogleUpdates _googleUpdates;



	/**
	 * On Api &gt;= 23 override on main activity and call this method
	 * @param requestCode request code provided in main onRequestPermissionsResult
	 * @param permissions permission provided in main onRequestPermissionsResult
	 * @param grantResults results provided in onRequestPermissionsResult
	 * @param context context, e.g getApplicationContext(), should not be null
	 */
	@TargetApi(23)//marshmallow
	public static void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults, Context context) {
		switch (requestCode) {
			case REQUEST_LOCATION:
			{

				_googleUpdates.asking = false;
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{

					try
					{
						if( _googleUpdates != null)
						{
							_googleUpdates.startLocationUpdates(false);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					TendartsClient.instance(context).onUserAcceptedLocationPermission();
				}
				else
				{
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					try
					{


						TendartsClient.instance(context).onUserRejectedLocationPermission();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}


	private  static boolean accessSent = false;
	/**
	 * override on main activity and call this method
	 */
	public static void onResume(final Context context) {

		LogHelper.logConsole("onResume, access sent: "+accessSent);
		if (_googleUpdates != null) {
			_googleUpdates.onResume();
		}


		if (Configuration.getAccessToken(context)== null) {

			return;
		}

		String pushCode = Configuration.instance(context).getPushCode();
		if (pushCode != null && ! accessSent) {
			accessSent = true;
			try {
				Communications.postData(String.format( Constants.DEVICE_ACCESS,
						Configuration.instance(context).getPushCode()),Util.getProvider(),  0, new ICommunicationObserver() {
					@Override
					public void onSuccess(int operationId, JSONObject data) {
						LogHelper.logConsole("sent device access");
						LogHelper.logEvent(context,"sent device access","");
					}

					@Override
					public void onFail(int operationId, String reason, Communications.PendingCommunication pc) {
						LogHelper.logConsole("could not send device access: "+reason);
						LogHelper.logEvent(context,"Can't device access",""+reason);

						Util.checkUnauthorized(reason,context);

						if(reason != null &&  reason.contains("404")) {
							String reg_id = Configuration.instance(context.getApplicationContext()).getPush();
							if( reg_id != null) {
								PushController.sendTokenAndVersion(reg_id, context.getApplicationContext(),
										true);
								//Log.i(TAG, "sending version");
							}
						}
					}
				}, "");
			}
			catch ( Exception e) {
				LogHelper.logConsole("send device access error: "+e.getMessage());
				TendartsClient.instance(context).remoteLogException(e);//todo mirar si se queda

				e.printStackTrace();
			}
		}
	}

	/**
	 * override on main activity and call this method
	 */
	public static void onPause() {
		try {
			if (_googleUpdates != null) {
				_googleUpdates.onPause();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * override on main activity and call this method
	 */
	public static void onStart() {
		try {

			try {
				Context weak = Configuration.getWeakContext();

				if( weak != null && Configuration.shouldSendGeostats(weak)) {
					if( Configuration.getAccessToken(weak)!= null) {
						Communications.sendGeoStats(Util.getProvider());
						Configuration.notifyGeostatsSent(weak);

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (_googleUpdates != null) {
				_googleUpdates.onStart();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * override on main activity and call this method
	 */
	public static void onStop() {
		try {
			if (_googleUpdates != null) {
				_googleUpdates.onStop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * override on main activity and call this method
	 */
	public static void onDestroy() {

		if( _googleUpdates!= null) {
			_googleUpdates.onDestroy();
		}

	}


	/**
	 * override on main activity and call this method
	 * @param savedInstanceState saved instance state
	 * @param activity instance of your main activity
	 * @param locationAlerter location alerter
	 */
	public static void onCreate(Bundle savedInstanceState,
								final Activity activity,
								final ILocationAlerter locationAlerter) {

		if (activity == null) {
			throw new InvalidParameterException("activity should not be null, pass your main activity");
		}
		initCommunications(activity.getApplicationContext());

		if (locationAlerter == null) {
			throw new InvalidParameterException("you should provide an instance that implements ILocationAlerter");
		}
		Communications.setLocationAlerter(new Communications.ILocationAlerter()
		{
			@Override
			public void alertNotEnabled(Activity parent)
			{
				locationAlerter.alertNotEnabled(parent);
			}
		});


		try {
			if (checkPlayServices(activity)) {
				// Start IntentService to register this application with GCM.
				Intent intent = new Intent(activity.getApplicationContext(), GCMRegistrationIntentService.class);
				activity.startService(intent);
			}
		} catch (Exception e) {
			Log.e("","Error requesting play services");

			e.printStackTrace();
		}

		try {
			_googleUpdates = new GoogleUpdates(activity.getApplicationContext(),activity);
			// Check for App Invite invitations and launch deep-link activity if possible.
			// Requires that an Activity is registered in AndroidManifest.xml to handle
			// deep-link URLs.

			boolean autoLaunchDeepLink = true;


		} catch (Exception e) {
			e.printStackTrace();
		}

		String reg_id = Configuration.instance(activity.getApplicationContext()).getPush();
		if (reg_id != null) {
			PushController.sendTokenAndVersion(reg_id, activity.getApplicationContext());
		}

		try {
			Date now = new Date();
			long elapsed = now.getTime() - lastHeartbeat.getTime();
			if (elapsed > 8600000) {
				//Log.d(TAG, "onCreate: sending gcm heartbeat");
				lastHeartbeat = now;
				activity.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
				activity.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (Configuration.shouldSendGeostats(activity.getApplicationContext())) {
				if (Configuration.getAccessToken(activity.getApplicationContext())!= null) {
					Communications.sendGeoStats(Util.getProvider());
					Configuration.notifyGeostatsSent(activity.getApplicationContext());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		PendingCommunicationsService.startPendingCommunications(activity.getApplicationContext());

		registerDartsReceiver(activity.getApplicationContext());

	}//onCreate


	public static void initCommunications(final Context context) {

		Communications.init(context );
	}


	//static boolean geostatsSent = false;


	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static boolean checkPlayServices( Activity activity ) {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity.getApplicationContext());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(resultCode)) {
				apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				//no google play
			}
			return false;
		}
		return true;
	}


	//------------------------------------------------------------------
	//			COMMANDS
	//------------------------------------------------------------------

	/**
	 * Delete a single notification from the stored list
	 * @param notificationCode notification code
	 * @param context context
	 */
	public static void deleteNotificationFromList(String notificationCode, Context context) {
		PersistentPush.removeById(notificationCode,context);
	}

	/**
	 * Delete all notifications from stored list
	 * @param context context
	 */
	public static void deleteAllNotifications(Context context)
	{
		PersistentPush.clear(context);
	}

	/**
	 * Returns a list with the stored notifications
	 * @param context
	 * @return the notification list
	 */
	public static ArrayList<Notification> getNotificationsList(Context context) {
		return PersistentPush.getStored(context);
	}

	/**
	 * open again the stacked notifications
	 * @param context
	 */
	public static void showStackedNotificationstNotification(Context context) {
		GCMListenerService.notifyList(context,
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE),
				null, PersistentPush.getStored(context));
	}

	/**
	 * Notify that a notification has been read
	 * @param notificationCode the code of the read notification
	 * @param context context
	 */
	public static void notifyNotificationRead(String notificationCode, final Context context) {

		if (Configuration.getAccessToken(context)== null) {
			return;
		}
		String json = Util.getDeviceJson(context);
		String url = String.format(Constants.PUSH_READ, notificationCode);
		LogHelper.logConsoleNet("sending pchd: "+url+"\n"+json);

		Communications.patchData(url,
				Util.getProvider(), 0, new ICommunicationObserver() {
					@Override
					public void onSuccess(int operationId, JSONObject data) {
						LogHelper.logEventPush(context,"succesfully notified read","");
						LogHelper.logConsole("push read notified");
					}

					@Override
					public void onFail(int operationId, String reason, Communications.PendingCommunication pending) {
						Util.checkUnauthorized(reason,context);
						LogHelper.logEventPush(context, "can't notify read",""+reason);
						LogHelper.logConsole("push read failed: "+reason);
						PendingCommunicationController.addPending(pending, context);
					}
				}, json,false);

		//todo: retries de parte del cliente o hacer cola???
	}


	/**
	 * Notify that all the notifications has been read
	 * @param context context
	 */
	public static void notifyAllNotificationsRead(final Context context) {
		if (Configuration.getAccessToken(context)== null) {
			return;
		}

		String json = Util.getDeviceJson(context);
		LogHelper.logConsoleNet("sending pchd: "+json);
		Communications.patchData(Constants.PUSH_ALL_READ,
				Util.getProvider(), 0, new ICommunicationObserver() {
					@Override
					public void onSuccess(int operationId, JSONObject data) {
						LogHelper.logEvent(context, "push all read notified","");
						LogHelper.logConsole("push all read notified");
					}

					@Override
					public void onFail(int operationId, String reason,
									   Communications.PendingCommunication pending) {
						Util.checkUnauthorized(reason,context);
						LogHelper.logEvent(context,"push all read failed",""+reason);
						LogHelper.logConsole("push all read failed: "+reason);
						PendingCommunicationController.addPending(pending, context);
					}
				}, json,false);
		//todo:retries de parte del cliente o hacer cola?
	}

	public static void onNewLocation() {
		if (geoReceiver == null ) {
			return;
		}
		IGeoLocationReceiver receiver = geoReceiver.get();
		if (receiver == null) {
			return;
		}

		receiver.onNewLocation( new GeoLocation(
				Communications.getLatitude(),
				Communications.getLongitude(),
				Communications.getLastPrecission(),
				Communications.getLastProvider(),
				Communications.getLastSource())
		);
	}

	public static String getDeviceCode(Context context) {
		return Configuration.instance(context).getPushCode();
	}

	/**
	 * Class that holds geolocation info
	 */
	public static class GeoLocation {
		/**
		 * Latitude
		 */
		public double latitude;

		/**
		 * Longitude
		 */
		public double longitude;

		/**
		 * precision in meters
		 * <p>
		 * We define accuracy as the radius of 68% confidence. In other
		 * words, if you draw a circle centered at this location's
		 * latitude and longitude, and with a radius equal to the accuracy,
		 * then there is a 68% probability that the true location is inside
		 * the circle.
		 *
		 * <p>In statistical terms, it is assumed that location errors
		 * are random with a normal distribution, so the 68% confidence circle
		 * represents one standard deviation. Note that in practice, location
		 * errors do not always follow such a simple distribution.
		 *
		 * <p>This accuracy estimation is only concerned with horizontal
		 * accuracy.
		 */
		public double precision;

		/**
		 * The provider of the location
		 */
		public String provider;
		/**
		 * location source
		 */
		public String source;

		/**
		 * Constructor
		 * @param latitude
		 * @param longitude
		 * @param precision
		 * @param provider
		 * @param source
		 */
		public GeoLocation(double latitude,
						   double longitude,
						   double precision,
						   String provider,
						   String source
						   ) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.precision = precision;
			this.provider = provider;
			this.source = source;
		}
	}

	/**
	 * Call this method to get the current geolocation
	 * @return the current geolocation
	 */
	public static GeoLocation getCurrentGeoLocation() {

		_googleUpdates.getLocationUpdates(true,false);

		return new GeoLocation(
				Communications.getLatitude(),
				Communications.getLongitude(),
				Communications.getLastPrecission(),
				Communications.getLastProvider(),
				Communications.getLastSource()
		);
	}

	/**
	 * A receiver that will be notified when a new location is available
	 */
	public interface IGeoLocationReceiver {
		/**
		 * New location arrived
		 * @param location the new location
		 */
		void onNewLocation(GeoLocation location);
	}

	private static WeakReference<IGeoLocationReceiver> geoReceiver;

	/**
	 * Set the Geolocation receiver, if there is already set, it will be overwritten with this one
	 * instance will not be kept, so make sure it's not garbage collected while you need it.
	 * @param receiver the receiver to be called
	 */
	public static void registerGeoLocationReceiver(IGeoLocationReceiver receiver)
	{
		geoReceiver = new WeakReference<IGeoLocationReceiver>(receiver);
	}

	/**
	 * Remove the geolocation receiver
	 */
	public static void unregisterGeoLocationReceiver()
	{
		geoReceiver = null;
	}

	/**
	 * Disable geolocation updates
	 */
	public static void disableGeolocationUpdates() {
		if (_googleUpdates != null) {
			_googleUpdates.onPause();
		}
	}

	/**
	 * Enable geolocation updates, by default are enabled
	 */
	public static void enableGeolocationUpdates() {
		if(_googleUpdates != null) {
			_googleUpdates.startLocationUpdates();
		}
	}


	/**
	 * Call this method to enable/disable notifications on current device/app
	 * @param enabled if the notifications should be enabled, by default are enabled
	 * @param context context
	 */
	public static void changeNotificationsEnabled(boolean enabled,Context context) {
		Configuration.instance(context).setNotificationsEnabled(enabled);
		onNotificationEnabledChange(enabled,context);
	}

	/**
	 * Get a boolean indicating if the notifications are enabled on current device/app
	 * @param context
	 * @return if the notifications are enabled
	 */
	public static  boolean getNotificationsEnabled(Context context) {
		boolean enabled =  Configuration.instance(context).getNotificationsEnabled();
		onNotificationEnabledChange(enabled,context);
		return enabled;
	}


	private static void onNotificationEnabledChange(final boolean isEnabled, final Context context) {
		Configuration.instance(context).setNotificationsEnabled(isEnabled);
		try {
			if (Configuration.getAccessToken(context)== null) {
				return;
			}

			//String data = "{ \"token\":\"" + Configuration.instance(context).getPush() + "\", \"platform\":0, \"disabled\":" + (isEnabled ? "false" : "true") +" }";

			JSONObject object = new JSONObject();
			String token = Configuration.instance(context).getPushCode();
			try {
				object.put("disabled",!isEnabled);
				Communications.addGeoData(object);
			} catch ( Exception e) {
				e.printStackTrace();
			}

			if (token == null) {
				LogHelper.logEventPush(context,"s" + "enabled config change not set due to device not registered yet",""+isEnabled);
				return;
			}

			LogHelper.logConsoleNet("PtcD: "+object);

			Communications.patchData(String.format( Constants.DEVICE,token),
					Util.getProvider(), 0, new ICommunicationObserver() {
				@Override
				public void onSuccess(int operationId, JSONObject data) {
					LogHelper.logEventPush(context,"successfully sent notification enabled config change",""+isEnabled);
					LogHelper.logConsole("notificationChange pushed" + data);
				}

				@Override
				public void onFail(int operationId, String reason, Communications.PendingCommunication pc)
				{
					Util.checkUnauthorized(reason,context);
					LogHelper.logEventPush(context,"error sending notification enabled config change",""+reason);
					LogHelper.logConsole("notificationChange failed" + reason);
				}
			}, object.toString(),false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Observer on  calls
	 */
	public interface IResponseObserver {
		/**
		 * Called when the operation has been  done correctly
		 */
		void onOk();

		/**
		 * Called when the operation failed
		 * @param reason reason of the fail, for development purposes, do not forward to user
		 */
		void onFail(String reason);
	}




	public static void notificationClicked(String notificationCode, final Context context) {

		if (Configuration.getAccessToken(context)== null) {
			return;
		}

		String json = Util.getDeviceJson(context);

		String url = String.format(Constants.PUSH_CLICKED, notificationCode);
		LogHelper.logConsoleNet("sending pchd: "+url+"\n"+json);
		Communications.patchData(url,
				Util.getProvider(), 0, new ICommunicationObserver() {
					@Override
					public void onSuccess(int operationId, JSONObject data) {
						LogHelper.logEventPush(context,"succesfully notified follow","");
						LogHelper.logConsole("push read notified");
					}

					@Override
					public void onFail(int operationId, String reason,
									   Communications.PendingCommunication pending) {
						Util.checkUnauthorized(reason,context);
						PendingCommunicationController.addPending(pending, context);
						LogHelper.logEventPush(context,"can't notify follow",""+reason);
						LogHelper.logConsole("push read failed: "+reason);
					}
				}, json,false);
	}


	static int linkRetries = 0;
	/**
	 * Links current device with your own user identifier
	 * @param observer
	 * @param context
	 * @param userIdentifier
	 */
	public static void linkDeviceWithUserIdentifier( final IResponseObserver observer,
													 final Context context,
													 String userIdentifier) {
		try {
			if (Configuration.getAccessToken(context)== null) {
				if (observer != null) {
					observer.onFail("SDK not properly initialized");
				}
				return;
			}

			String code = Configuration.instance(context).getPushCode();
			if (code == null || code.length() < 3) {
				PendingCommunicationController.setPendingLink(userIdentifier, context);
				String push = Configuration.instance(context).getPush();
				if (push != null && push.length() >3) {
					PushController.sendTokenAndVersion(push,context);
				}
				if(observer != null) {
					observer.onFail("Device not registered yet, try a few seconds later");
				}
				return;
			}

			final JSONObject object = new JSONObject();
			object.put("client_data",userIdentifier);
			String deviceId = String.format( Constants.DEVICE_REFERENCE,Configuration.instance(context).getPushCode());

			object.put("device",deviceId );

			Communications.postData(Constants.LINKS, Util.getProvider(), 0, new ICommunicationObserver() {
				@Override
				public void onSuccess(int operationId, JSONObject data) {
					if (data.has("persona") && !data.isNull("persona")) {
						try {
							String resourceUri = data.getString("persona");
							Configuration.instance(context).setUserCode(resourceUri);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (observer != null) {
						observer.onOk();
					}
				}

				@Override
				public void onFail(int operationId, String reason,
								   Communications.PendingCommunication pc) {
					PendingCommunicationController.addPending(pc,context);
					Util.checkUnauthorized(reason,context);
					if (observer != null) {
						observer.onFail(reason);
					}

				}
			},object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}



	/**
	 * Call this to link the user to the device, call it after the user has been set
	 * @param observer observer to be called when the operation is done
	 * @param context context
	 */
	public static void linkUserToDevice(final IResponseObserver observer, final Context context) {
		if (observer == null) {
			throw new InvalidParameterException("observer can't be null");
		}

		if (Configuration.getAccessToken(context)== null) {
			if (observer != null) {
				observer.onFail("SDK not properly initialized");
			}
			return;
		}

		String user = Configuration.instance(context).getUserCode();
		if (user == null ) {
			throw  new IllegalStateException("the user should be already registered");
		}

		try {

			final JSONObject object = new JSONObject();
			String token = Configuration.instance(context).getPushCode();
			if (token == null) {
				observer.onFail("device not yet registered");
				return;
			}
			try {

				object.put("persona",user);//String.format(Constants.RELATIVE_USER, USER));
				Communications.addGeoData(object);

			} catch ( Exception e) {
				e.printStackTrace();
			}

			LogHelper.logConsoleNet("PtcD: "+object);

			Communications.patchData(String.format( Constants.DEVICE,token),
					Util.getProvider(), 0, new ICommunicationObserver() {
				@Override
				public void onSuccess(int operationId, JSONObject data) {
					observer.onOk();
					LogHelper.logConsole("device linked" + data);
				}

				@Override
				public void onFail(int operationId, String reason, Communications.PendingCommunication pc) {
					Util.checkUnauthorized(reason,context);
					observer.onFail(reason);

					LogHelper.logConsoleNet("device link failed" + reason);
				}
			}, object.toString(),false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * Called to register the user
	 * @param email e-mail
	 * @param firstName first name
	 * @param lastName last name
	 * @param password password to use
	 * @param observer observer to be called when the operation is done
	 * @param context context
	 */
	public static void registerUser(String email, String firstName, String lastName, String password,
					  final IResponseObserver observer,
									final Context context) {
		if (email == null || email.length() <4 || !email.contains("@")) {
			throw new InvalidParameterException("provide a valid email");
		}
		if (observer == null) {
			throw new InvalidParameterException("provide an observer");
		}

		if (Configuration.getAccessToken(context)== null) {
			if (observer != null) {
				observer.onFail("SDK not properly initialized");
			}
			return;
		}

		final JSONObject object = new JSONObject();
		try {
			object.put("email",email);
			if (firstName != null && firstName.length() > 0) {
				object.put("first_name", firstName);
			}
			if (lastName != null && lastName.length() > 0) {
				object.put("last_name",lastName);
			}
			if (password != null && password.length() > 0) {
				object.put("password", password);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		LogHelper.logConsoleUser("sending registration: "+object);

		Communications.postData(Constants.REGISTER_USER,
				Util.getProvider(), 0, new ICommunicationObserver() {
			@Override
			public void onSuccess(int operationId, JSONObject data) {
				LogHelper.logConsoleUser("successfuly registered:"+data);
				observer.onOk();
			}

			@Override
			public void onFail(int operationId, String reason, Communications.PendingCommunication pc) {
				Util.checkUnauthorized(reason,context);
				LogHelper.logConsoleUser("failed registration: "+reason);
				observer.onFail(reason);
			}
		},object.toString(),false);

	}

	/**
	 * Delayed intit
	 * @param accessToken
	 * @param gcmSenderId
	 * @param clientClassName
	 * @param context
	 */
	public static  void delayedInit( String accessToken,String gcmSenderId,String clientClassName, Context context) {
		TendartsClient.refreshInstance();
		Configuration.instance(context).setSoftAccessToken(accessToken);
		Configuration.instance(context).setAccessToken(accessToken);
		Configuration.instance(context).setSoftClientClassName(clientClassName);
		Configuration.instance(context).setSoftGCMDefaultSenderId(gcmSenderId);

		//fire registration
		Intent intent = new Intent(context.getApplicationContext(), GCMRegistrationIntentService.class);
		context.startService(intent);
	}

	/**
	 * Modify user data
	 *
	 * only fill the fields you want to modify, any null parameter will be kept unmodified
	 * @param email e-mail
	 * @param firstName first name
	 * @param lastName last name
	 * @param password password
	 * @param observer observer to be called when the operation is done
	 */
	public static void modifyUser(String email, String firstName, String lastName, String password,
								  final IResponseObserver observer, final Context context) {

		if (observer == null) {
			throw new InvalidParameterException("provide an observer");
		}

		if (Configuration.getAccessToken(context)== null) {
			if (observer != null) {
				observer.onFail("SDK not properly initialized");
			}
			return;
		}

		if (Configuration.instance(context).getUserCode() == null) {
			observer.onFail("User not yet registered, please register first");
			return;
		}
		//todo: asignar errores numericos a toda la casuistica

		String url = Configuration.instance(context).getUserCode();//String.format(Constants.USER,);

		final JSONObject object = new JSONObject();
		try {
			object.put("email",email);
			if (firstName != null && firstName.length() > 0) {
				object.put("first_name", firstName);
			}
			if (lastName != null && lastName.length() > 0) {
				object.put("last_name",lastName);
			}
			if (password != null && password.length() > 0) {
				object.put("password", password);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		LogHelper.logConsoleUser("sending modification: "+object);

		Communications.patchData(url,
				Util.getProvider(), 0, new ICommunicationObserver() {
			@Override
			public void onSuccess(int operationId, JSONObject data) {
				LogHelper.logConsoleUser("successfuly modified:"+data);
				observer.onOk();
			}

			@Override
			public void onFail(int operationId, String reason, Communications.PendingCommunication pc) {
				Util.checkUnauthorized(reason,context);
				LogHelper.logConsoleUser("failed modification: "+reason);
				observer.onFail(reason);
			}
		},object.toString(),false);

	}

	/**
	 * Associates Key-Value with device
	 * @param context
	 * @param key
	 * @param kind
	 * @param value
	 * @param observer
	 */
	public static void associateKeyValueWithDevice(@NonNull Context context,
												   @NonNull String key,
												   @Nullable TDKeysHandler.KeyValueKind kind,
												   @Nullable String value,
												   @Nullable final IResponseObserver observer) {

		TDKeysHandler.associateKeyValueWithDevice(context, key, kind, value, new TDKeysHandler.TDKeysHandlerInterface() {
			@Override
			public void onSuccess() {
				if (observer != null) {
					observer.onOk();
				}
			}

			@Override
			public void onError(String reason) {
				if (observer != null) {
					observer.onFail(reason);
				}
			}
		});

	}

	/**
	 * Associates Key-Value with user
	 * @param context
	 * @param key
	 * @param kind
	 * @param value
	 * @param observer
	 */
	public static void associateKeyValueWithUser(@NonNull Context context,
												 @NonNull String key,
												 @Nullable TDKeysHandler.KeyValueKind kind,
												 @Nullable String value,
												 @Nullable final IResponseObserver observer) {

		TDKeysHandler.associateKeyValueWithUser(context, key, kind, value, new TDKeysHandler.TDKeysHandlerInterface() {
			@Override
			public void onSuccess() {
				if (observer != null) {
					observer.onOk();
				}
			}

			@Override
			public void onError(String reason) {
				if (observer != null) {
					observer.onFail(reason);
				}
			}
		});

	}

	/**
	 * Location alerter to redirect user to configuration
	 */
	public interface ILocationAlerter {
		/**
		 * Alert the user that location is not enabled, good place to open device location settings
		 * for the user
		 * @param parent could be null.
		 */
		void alertNotEnabled(Activity parent);
	}

	private static void registerDartsReceiver(Context context) {

		DartsReceiver dartsReceiver = new DartsReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(DartsReceiver.CLEAR_PUSHES);
		filter.addAction(DartsReceiver.OPEN_PUSH);
		filter.addAction(DartsReceiver.OPEN_LIST);
		filter.addAction(DartsReceiver.NOTIFICATION_ACTION);
		context.registerReceiver(dartsReceiver, filter);
	}

	/**
	 * Set up a map containing the pairs:
	 * 	("Action to be filtered for your BroadcastReceiver", "Command of the push notification action")
	 *
	 * 	TenDarts will send to that BroadcastReceiver the event consisting on
	 * 	receiving a push notification action tap
	 *
	 * @param context
	 * @param map
	 */
	public static void setReplyActions(Context context, Map<String, String> map) {
		Configuration.instance(context).setReplyActionsMap(map);
	}

}
