package com.tendarts.sdk.client;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.tendarts.sdk.Model.Notification;
import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.common.LogHelper;

/**
 * Created by jorgearimany on 8/4/17.
 */

class TendartsClientDefaultImplementation extends TendartsClient
{


	private static final String TAG = "SDKClientDefaultImpl.";

	TendartsClientDefaultImplementation()
	{
		super();
	}

	/**
	 * @param notification
	 */
	@Override
	public void onNotificationShowed(Notification notification)
	{

	}

	/**
	 * @param notification
	 */
	@Override
	public void onNotificationShowedInList(Notification notification)
	{

	}

	@Override
	public boolean onNotificationReceived(Notification push)
	{
		return false;
	}

	@Override
	public boolean onNotificationClicked(Notification push)
	{
		return false;
	}


	@Override
	public boolean onNotificationListCleared()
	{
		return false;
	}

	/**
	 * Open the notification, start your desired intent configured properly
	 *
	 * @param notification the clicked notification
	 */
	@Override
	public boolean openNotification(Notification notification, Context context)
	{
		return false;
	}

	/**
	 * Start the proper intent to show notification list, the user has clicked on stacked notification
	 */
	@Override
	public void openNotificationList(Context context)
	{

	}

	/**
	 * To be overridden, e.g.: Crashlytics.logException(e);
	 * @param e
	 */
	public void remoteLogException(Exception e)
	{
		Log.e(TAG, "Exception: ", e);
	}

	/**
	 *  To be overriden, e.g.:
	 *  Tracker _t = OnpublicoApplication.getPushTracker();
	 *	if (_t != null)
	 *	{
	 *		_t.send(new HitBuilders.EventBuilder().setCategory("Push")
	 *		.setNonInteraction(true)
	 *		.setAction("errorRegister").setLabel(e.getMessage()).build());
	 *	}
	 *
	 *
	 * @param category category
	 * @param type type
	 * @param message message
	 */
	public void logEvent(String category, String type, String message) {
		LogHelper.logConsole(TAG, "logEvent: c:"+category+" t:"+type+" m:"+message);
	}


	/**
	 * Do any customization here like Sdk.alwaysShowLastNotification:
	 * Sdk.alwaysShowLastNotification(false);
	 */
	@Override
	public void performSetup(Context context)
	{

		TendartsSDK.instance().alwaysShowLastNotification(true);

	}

	/**
	 * Called to retrieve the main activity class name
	 * {@code
	 * return MainActivity.class.getName()
	 * }
	 *
	 * @return the main activity class name
	 */
	@Override
	public String mainActivityClassName()
	{
		return null;
	}


	//----------------------------------------------------------------------------------------------
	//						NOTIFICATIONS
	//----------------------------------------------------------------------------------------------


	/**
	 * to be overridden, return true if the notification should have an image
	 * @param push push containing data to build the notification
	 * @return true if the notification should contain a custom image.
	 */
	@Override
	public boolean notificationHasImage(Notification push)
	{
		//todo: extract to onpublico and return false
		String dst = push.getString("dst");
		if( dst != null)
		{
			String code = push.getString("dsc");
			if( code == null)
			{

				return false;
			}

			switch (dst)
			{
				case "usr":
				case "n":
				case "evt":
					return true;

				default:
					return false;
			}
		}

		return false;
	}


	/**
	 * if you need to load some data could be done later, return immediately
	 * @param push
	 * @param context
	 * @return
	 */
	@Override
	public RemoteViews getCustomNotificationSmallView(Notification push, Context context)
	{
		//todo extract to onpublico and return null
		/*
		if( push.hasImage(context))
		{

			try
			{

				String date =
						Utils.dateToStringShort(context,
								new Date(),
								context.getString(R.string.yesterday),
								context.getString(R.string.tomorrow));

				rv = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
				rv.setTextViewText(R.id.header,title);
				rv.setTextViewText(R.id.content,message);
				rv.setTextViewText(R.id.timestamp, date);

				rv.setImageViewResource(R.id.avatar, R.mipmap.ic_op_round_red);

				//builder.setContent(rv);
				//	builder.setCustomBigContentView(rv);



				rv2 = new RemoteViews(context.getPackageName(), R.layout.notification_custom_big);
				rv2.setTextViewText(R.id.header,title);
				rv2.setTextViewText(R.id.content,message);
				rv2.setTextViewText(R.id.timestamp, date);
				//builder.setCustomBigContentView(rv2);
				//notification.bigContentView = rv;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if( push.isAuthor())
			{
				rv = null;
				rv2 = null;
			}
		}*/
		return null;

	}

	/**
	 * you should also return a view on getCustomNotificationSmallView
	 * if you need to load some data could be done later, return immediately
	 * @param push
	 * @param context
	 * @return
	 */
	@Override
	public RemoteViews getCustomNotificationLargeView(Notification push, Context context)
	{

		//todo extract to onpublico and return null
		/*
		push.hasImage(context)

					try
					{

						String date =
								Utils.dateToStringShort(context,
										new Date(),
										context.getString(R.string.yesterday),
										context.getString(R.string.tomorrow));

						rv = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
						rv.setTextViewText(R.id.header,title);
						rv.setTextViewText(R.id.content,message);
						rv.setTextViewText(R.id.timestamp, date);

						//builder.setContent(rv);
						//	builder.setCustomBigContentView(rv);



						rv2 = new RemoteViews(context.getPackageName(), R.layout.notification_custom_big);
						rv2.setTextViewText(R.id.header,title);
						rv2.setTextViewText(R.id.content,message);
						rv2.setTextViewText(R.id.timestamp, date);
						//builder.setCustomBigContentView(rv2);
						//notification.bigContentView = rv;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					if( push.isAuthor())
					{
						rv = null;
						rv2 = null;
					}

					rvBig.setImageViewResource(R.id.avatar,R.mipmap.ic_op_round_red);
		 */
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

	@Override
	public CharSequence getLocationExplanation(Context context)
	{
		return "...";
	}

	@Override
	public void onUserRejectedLocationPermission()
	{

	}

	@Override
	public void onUserAcceptedLocationPermission()
	{

	}
}
