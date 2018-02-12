package com.tendarts.sdk.client;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.widget.RemoteViews;

import com.tendarts.sdk.Model.Notification;
import com.tendarts.sdk.Model.PersistentPush;
import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.common.Configuration;

import java.security.InvalidParameterException;

/**
 *  You should subclass SDKClient and implement the interfaces (mysdkClient)
 *  Please add  tendarts_sdk_client_class:\\\"com.yourcompany.YourClientClass\\\" in manifestPlaceholders
*
 */
public abstract   class TendartsClient extends BroadcastReceiver implements INotifications, ISDKCore, IUIStrings, IGeolocation
{
	private static final String TAG = "SDKClient";

	private static TendartsClient _me;


	public TendartsClient()
	{

	}



	public final  static void refreshInstance()
	{
		_me = null;
	}

	public final static TendartsClient instance(Context context)
	{
		return instance(context.getApplicationContext(),null);
	}

	public final static TendartsClient instance(Context context, ApplicationInfo info)
	{
		Context c = context;


		if( context != null)
		{
			c = context.getApplicationContext();
		}
		if( _me == null)
		{
			Log.d(TAG, "instance: creating from context");
			if( c == null)
			{
				Log.w(TAG, "instance: no context!!!!!" );
				return new TendartsClientDefaultImplementation();

			}

			String name = null;
			if( info != null)
			{
				Log.d(TAG, "instance: initiating from "+ info);
				name = Configuration.getClientClassName(c,info	);
			}
			else
			{
				name = Configuration.getClientClassName(c);
			}
			if( name != null && name.length()>0)
			{

				Log.d(TAG, "client class: "+name);
				try
				{
					_me = (TendartsClient) Class.forName(name).newInstance();
					_me.performSetup(context);
				} catch (InstantiationException e)
				{
					e.printStackTrace();
					Log.e("DartsSDK:Config", "Please add  tendarts_sdk_client_class:\\\"com.yourcompany.YourClientClass\\\" in manifestPlaceholders");

					throw new InvalidParameterException("Error instantiating "+name);
				} catch (IllegalAccessException e)
				{
					e.printStackTrace();
					Log.e("DartsSDK:Config", "Please add  tendarts_sdk_client_class:\\\"com.yourcompany.YourClientClass\\\" in manifestPlaceholders");
					throw  new InvalidParameterException("Illegal Access instantiating "+name);
				} catch (ClassNotFoundException e)
				{
					e.printStackTrace();
					Log.e("DartsSDK:Config", "Please add  tendarts_sdk_client_class:\\\"com.yourcompany.YourClientClass\\\" in manifestPlaceholders");
					throw new InvalidParameterException("could not find "+name);
				}
				catch (Exception e)
				{
					throw new InvalidParameterException("Exception instantiating "+name);
				}
			}
			else
			{
				Log.w(TAG, "could not get client class: "+name);
			}
			if( _me == null)
			{
				_me = new TendartsClientDefaultImplementation();
			}
		}
		return _me;
	}



	@Override
	public final void onReceive(Context context, Intent intent)
	{
		if(intent!=null)
		{

			String action = intent.getAction();


			if ("com.darts.sdk.CLEAR_PUSHES".equalsIgnoreCase(action))
			{

				PersistentPush.clear(context);
				Log.i(TAG,"clearing push list");
				if( intent.hasExtra("dismiss"))
				{
					int id = intent.getIntExtra("dismiss", -1);
					if( id != -1)
					{

						NotificationManager manager =
								(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						manager.cancel(id);
						//manager.cancel(PushController.SINGLE_ID);// ¿cancell all???

					}
				}

				try
				{
					logEvent("Push","clear list from push", "");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				onNotificationListCleared();
				return;



			}

		}
	}

	/**
	 * Called when the user has rejected location permission, do not make any further UI interaction with the user regarding location permission
	 */
	@Override
	public void onUserRejectedLocationPermission()
	{

	}

	/**
	 * Called when the user has accepted location permission, note that asking for permissions depends on several factors and is not always asked to the user.
	 */
	@Override
	public void onUserAcceptedLocationPermission()
	{

	}

	/**
	 * To send a remote exception to your desired server
	 * e.g.: {@code Crashlytics.logException(e);}
	 *
	 * @param e the exception to be logged
	 */
	@Override
	public void remoteLogException(Exception e)
	{

	}

	/**
	 * Called to log events, for example to use analytics
	 * {@code
	 * Tracker _t = OnpublicoApplication.getPushTracker();
	 * if (_t != null)
	 * {
	 * _t.send(new HitBuilders.EventBuilder().setCategory("Push")
	 * .setNonInteraction(true)
	 * .setAction("errorRegister").setLabel(e.getMessage()).build());
	 * }
	 * }
	 *
	 * @param category category
	 * @param type     type
	 * @param message  message
	 */
	@Override
	public void logEvent(String category, String type, String message)
	{

	}

	/**
	 * Called when the SDK is initialized
	 * <p>
	 * Do any customization here like Sdk.alwaysShowLastNotification:
	 * <p>
	 * {@code Sdk.alwaysShowLastNotification(false);}
	 *
	 * @param context context to be used, for example to get string resources
	 */
	@Override
	public void performSetup(Context context)
	{

	}



	/**
	 * Called when a notification is showed to the user
	 *
	 * @param notification
	 */
	@Override
	public void onNotificationShowed(Notification notification)
	{

	}

	/**
	 * Called when a notification is showed to the user in the stacked notification
	 *
	 * @param notification
	 */
	@Override
	public void onNotificationShowedInList(Notification notification)
	{

	}

	/**
	 * Called when a notification is received in the device
	 *
	 * @param notification
	 * @return
	 */
	@Override
	public boolean onNotificationReceived(Notification notification)
	{
		return false;
	}

	/**
	 * Called when a showed notification is clicked
	 *
	 * @param notification
	 * @return
	 */
	@Override
	public boolean onNotificationClicked(Notification notification)
	{
		return false;
	}

	/**
	 * Called when the notification list is cleared
	 *
	 * @return
	 */
	@Override
	public boolean onNotificationListCleared()
	{
		return false;
	}

	/**
	 * Open the notification, start your desired intent configured properly, return true
	 * <p>
	 * to override deep url launching.
	 *
	 * @param notification the clicked notification
	 * @param context      context to be used
	 * @return true if client opens the notification overriding SDK default behabiour
	 */
	@Override
	public boolean openNotification(Notification notification, Context context)
	{
		return false;
	}

	/**
	 * Start the proper intent to show notification list, the user has clicked on stacked notification
	 *
	 * @param context context to be used
	 */
	@Override
	public void openNotificationList(Context context)
	{

	}

	/**
	 * Return true if the notification should have an image
	 *
	 * @param notification notification containing data to build the notification
	 * @return true if the notification should contain a custom image.
	 */
	@Override
	public boolean notificationHasImage(Notification notification)
	{
		return false;
	}

	/**
	 * if you need to load some data could be done later, return immediately,
	 * override if you want to provide your custom notification view when collapsed
	 *
	 * @param notification
	 * @param context
	 * @return
	 */
	@Override
	public RemoteViews getCustomNotificationSmallView(Notification notification, Context context)
	{
		return null;
	}

	/**
	 * you should also return a view on getCustomNotificationSmallView
	 * if you need to load some data could be done later, return immediately
	 * override if you want to provide your large custom notification view when expanded
	 *
	 * @param notification
	 * @param context
	 * @return
	 */
	@Override
	public RemoteViews getCustomNotificationLargeView(Notification notification, Context context)
	{
		return null;
	}

	/**
	 * Called to start all your background custom notification loading, return immediately.
	 * <p>
	 * You MUST call listener.revertToStandardNotification() or listener.customNotificationsReady()
	 * once.
	 *
	 * @param listener     listener to call when you are ready
	 * @param notification notification to load data
	 * @param rv           RemoteViews to be filled
	 * @param rvBig        RemoteViews to be filled
	 * @param context      context to use
	 */
	@Override
	public void loadBackgroundCustomNotificationData(IBackgroundCustomNotificationLoaderListener listener, Notification notification, RemoteViews rv, RemoteViews rvBig, Context context)
	{

	}

	/**
	 * Return a CharSequence explaining to the user why your app uses location and why permission should be granted.
	 * the returned value should be multilanguage aware.
	 *
	 * @param context context to be used to access string resources
	 * @return the message to be shown to the user when required in the correct language
	 */
	@Override
	public CharSequence getLocationExplanation(Context context)
	{
		return null;
	}


	/**
	 * If linkDeviceWithUserIdentifier has failed, an automatic retry is started, so when finally
	 * the user is linked this method will be called
	 */

	@Override
	public  void onUserLinkedToDevice()
	{

	}

	//throw new UnsupportedOperationException("Not yet implemented: derive from SDKClient and override getGCMDefaultSenderId");

}
