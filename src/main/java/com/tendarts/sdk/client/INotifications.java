package com.tendarts.sdk.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.tendarts.sdk.Model.Notification;

/**
 * Notifications related interface
 */

public interface INotifications
{


	/**
	 * Called when a notification is showed to the user
	 * @param notification
	 */
	void onNotificationShowed(Notification notification);

	/**
	 * Called when a notification is showed to the user in the stacked notification
	 * @param notification
	 */
	void onNotificationShowedInList(Notification notification);
	/**
	 * Called when a notification is received in the device
	 * @param notification
	 * @return
	 */
	boolean onNotificationReceived(Notification notification);

	/**
	 * Called when a showed notification is clicked
	 * @param notification
	 * @return
	 */
	boolean onNotificationClicked(Notification notification);



	/**
	 * Called when the notification list is cleared
	 * @return
	 */
	boolean onNotificationListCleared();


	/**
	 * Open the notification, start your desired intent configured properly, return true
	 *
	 * to override deep url launching.
	 * @param notification the clicked notification
	 * @param context  context to be used
	 * @return true if client opens the notification overriding SDK default behabiour
	 */
	boolean openNotification(Notification notification, Context context);


	/**
	 * Start the proper intent to show notification list, the user has clicked on stacked notification
	 * @param context context to be used
	 */
	void openNotificationList(Context context);

	/**
	 * Return true if the notification should have an image
	 * @param notification notification containing data to build the notification
	 * @return true if the notification should contain a custom image.
	 */
	public boolean notificationHasImage(Notification notification);


	/**
	 * if you need to load some data could be done later, return immediately,
	 * override if you want to provide your custom notification view when collapsed
	 * @param notification
	 * @param context
	 * @return
	 */
	public RemoteViews getCustomNotificationSmallView(Notification notification, Context context);


	/**
	 * you should also return a view on getCustomNotificationSmallView
	 * if you need to load some data could be done later, return immediately
	 *  override if you want to provide your large custom notification view when expanded
	 * @param notification
	 * @param context
	 * @return
	 */
	public RemoteViews getCustomNotificationLargeView(Notification notification, Context context);




	/**
	 * To provide asynchronous custom notifications loading.
	 *
	 * You must call either revertToStandardNotification or customNotificationsReady once
	 */
	public interface IBackgroundCustomNotificationLoaderListener
	{
		/**
		 * Loading failed and should fallback to standard notification
		 */
		void revertToStandardNotification();

		/**
		 * All asynchronous loading and manipulations done, remote views are ready to use
		 * @param bitmap if provided this bitmap will be used in NotificationBuilder..setLargeIcon()
		 */
		void customNotificationsReady(Bitmap bitmap);
	}

	/**
	 * Called to start all your background custom notification loading, return immediately.
	 *
	 * You MUST call listener.revertToStandardNotification() or listener.customNotificationsReady()
	 * once.
	 * @param listener listener to call when you are ready
	 * @param notification notification to load data
	 * @param rv RemoteViews to be filled
	 * @param rvBig RemoteViews to be filled
	 * @param context  context to use
	 *
	 */
	void loadBackgroundCustomNotificationData(final IBackgroundCustomNotificationLoaderListener listener,
											  final Notification notification,
											  final RemoteViews rv,
											  final RemoteViews rvBig,
											  Context context);

}
