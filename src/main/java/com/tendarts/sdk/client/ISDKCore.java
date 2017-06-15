package com.tendarts.sdk.client;

import android.content.Context;

/**
 * ISDKCore handles core events on SDK
 */

public interface ISDKCore
{
	/**
	 * To send a remote exception to your desired server
	 * e.g.: {@code Crashlytics.logException(e);}
	 * @param e the exception to be logged
	 */
	public void remoteLogException(Exception e);

	/**
	 *  Called to log events, for example to use analytics
	 *  {@code
	 *  Tracker _t = OnpublicoApplication.getPushTracker();
	 *	if (_t != null)
	 *	{
	 *		_t.send(new HitBuilders.EventBuilder().setCategory("Push")
	 *		.setNonInteraction(true)
	 *		.setAction("errorRegister").setLabel(e.getMessage()).build());
	 *	}
	 *	}
	 *
	 *
	 * @param category category
	 * @param type type
	 * @param message message
	 */
	public void logEvent(String category, String type, String message);


	/**
	 * Called when the SDK is initialized
	 *
	 * Do any customization here like Sdk.alwaysShowLastNotification:
	 *
	 * {@code Sdk.alwaysShowLastNotification(false);}
	 *
	 * @param context context to be used, for example to get string resources
	 */
	public void performSetup( Context context);

	/**
	 * Called to retrieve the main activity class name
	 * {@code
	 *  return MainActivity.class.getName()
	 * }
	 * @return the main activity class name
	 */
	public String mainActivityClassName();
}
