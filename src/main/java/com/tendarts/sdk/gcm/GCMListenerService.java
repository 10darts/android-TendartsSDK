package com.tendarts.sdk.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.RemoteViews;

import com.tendarts.sdk.communications.Communications;
import com.tendarts.sdk.communications.ICommunicationObserver;
import com.tendarts.sdk.communications.IImageDownloadObserver;
import com.google.android.gms.gcm.GcmListenerService;

import com.tendarts.sdk.Model.PersistentPush;
import com.tendarts.sdk.Model.Push;
import com.tendarts.sdk.Model.Notification;
import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.client.INotifications;
import com.tendarts.sdk.client.TendartsClient;
import com.tendarts.sdk.common.Configuration;
import com.tendarts.sdk.common.Constants;
import com.tendarts.sdk.common.PushController;
import com.tendarts.sdk.common.Util;


import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;


/**
 * Created by jorgearimany on 6/4/17.
 */

public class GCMListenerService extends GcmListenerService
{
	private static final String TAG = "GCM Listener";
	public static int not_id = PushController.NOTIFICATION_ID;


	//todo cambiar single_id por id push cuando no stacked
	static Push.IImageUrlObserver _imageObserver;

	static TendartsClient.IBackgroundCustomNotificationLoaderListener _backgroundListener;
	public void onMessageReceived(String from, Bundle data)
	{
		Context context = getApplicationContext();
		if( context == null)
		{
			context = this;
		}
		TendartsSDK.instance().initCommunications(context);
		super.onMessageReceived(from, data);
		Log.i(TAG, "Message received:" + from);
		Util.printExtras(TAG, data);


		Bundle extras =data;


		try
		{



			try
			{


				//----- check metadata
				final String id = extras.getString("id");
				String str = extras.getString("cfm");

				int report =  0;

				if( "1".equalsIgnoreCase(str))
				{
					report = 1;
				}
				else
				{
					report = extras.getInt("cfm");
				}

				String origin = extras.getString("org");
				if( !"10d".equals(origin))
				{
					TendartsClient.instance(getApplicationContext()).remoteLogException(
							new Exception("invalid origin received: "+origin));
					return;
				}

				//---- device check:
				/*
				try
				{
					final String did = extras.getString("dvc");
					if ( did != null )
					{
						String mydid = Configuration.instance(getApplicationContext()).getPushCode();
						if (mydid != null )
						{
							if (!mydid.equalsIgnoreCase(did))
							{
								Communications.putData(
										Constants.disablePush + URLEncoder.encode(did, "utf-8") + "/",
										Util.getProvider(), 0, new ICommunicationObserver()
										{
											@Override
											public void onSuccess(int operationId, JSONObject data)
											{
												Log.i(TAG, "patched device" + did);
											}

											@Override
											public void onFail(int operationId, String reason)
											{
												Log.w(TAG, "failed to patch device" + did);
											}
										}, "", false);
								String message = "push:dvc = " + did + " id:" + id + " device:" + mydid +
										"sending disable on " + did;

								SDKClient.instance(getApplicationContext(), getApplicationInfo()).remoteLogException(new Exception(message));
								//Crashlytics.logException(new Exception(message));

								try
								{
									SDKClient.instance(getApplicationContext(), getApplicationInfo()).logEvent("Push","repeated_id",extras.getString("message"));
								} catch (Exception e)
								{
									e.printStackTrace();
								}
								return;//not for user
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
*/


				//---- send recived
				try
				{


					TendartsClient.instance(context, getApplicationInfo()).logEvent("Push","received", extras.getString("message"));

					/*Tracker _t = OnpublicoApplication.getPushTracker();
					if( _t != null)
					{
						_t.send(new HitBuilders.EventBuilder().setCategory("Push")
								.setNonInteraction(true)
								.setAction("received").setLabel("" + extras.getString("message")).build());
						Log.i(TAG, "Push sent to analytics:" + id + " " + extras.getString("message"));
					}*/
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				//---- report receive if needed
				Log.d(TAG, str + " " + report);
				if( id != null && report == 1)
				{
					String json = null;
					json = Util.getDeviceJson(context);


					if( json != null)
					{
						final Context finalContext = context;

						Communications.patchData(
								String.format(Constants.pushReceived,
										id
										),
								Util.getProvider(), 0, new ICommunicationObserver()
								{
									@Override
									public void onSuccess(int operationId, JSONObject data)
									{
										TendartsClient.instance(finalContext).
												logEvent("PUSH","push received successfully reported","");
										Log.i(TAG, "push rcv reported");
									}

									@Override
									public void onFail(int operationId, String reason)
									{
										Util.checkUnauthorized(reason,getApplicationContext());
										TendartsClient.instance(finalContext).
												logEvent("PUSH","push received report can't send:",""+reason);

										if( reason != null &&reason.contains("400"))
										{
											//debug, borrar
											TendartsClient.instance(finalContext).
													logEvent("PUSH","push received send 400:",""+
											reason + " duplicate: sent id:" + id + " stored id's:"
															+ PersistentPush.getAllIds(finalContext));
										}


										Log.w(TAG, "push rcv failed: " + reason);
									}
								}, json, false);
					}
				}
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}



			postNotification( context, extras);




		}
		catch (Exception e)
		{
			e.printStackTrace();
		}




	}




	public  void postNotification(final Context context, Bundle extras)
	{

		try
		{
			int count =0;
			if( extras.containsKey("badge"))
			{
				Log.d(TAG, "postNotification: new badge");
				count = Integer.parseInt(extras.getString("badge"));
			}
			else
			{
				Log.d(TAG, "postNotification: no badge");
			}
			Util.setBadgeCount(count, context);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}



		try
		{

			String message = extras.getString("message").trim();

			try
			{
				if (!Configuration.instance(context.getApplicationContext()).getNotificationsEnabled())
				{
					try
					{

						TendartsClient.instance(context).logEvent("Push","config_disabled","" + message);

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					return;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}


			TendartsClient.instance(context).onNotificationReceived(buildPushFromBundle(extras));


			final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);





			if( message == null || message.length() <1)
			{
				try
				{
					Log.e(TAG, "null message...");


					TendartsClient.instance(context).logEvent("Push","no_message",""+extras.getString("id"));
					StringBuilder all = new StringBuilder("Extras: ");
					for (String key : extras.keySet())
					{
						Object value = extras.get(key);
						all.append(key).append(": ").append(value).append(";");
					}
					TendartsClient.instance(context).logEvent("Push","all_no_message", all.toString());

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}

			final String originalMessage = message;

			String title = extras.getString("title");


			if (title == null || title.length() < 1)
			{
				int index = message.indexOf(":");
				if( index >0 && index < message.length()-2)
				{
					title = message.substring(0,index+1);
					message = message.substring(index+1);

				}
				else
				{
					//title = "Onpublico";
				}
			}

			final Notification push = buildPushFromBundle(extras);


			boolean showed = false;
			if( PersistentPush.alreadyContains(push, context))
			{
				showed = true;
				Log.d(TAG, "postNotification: pust already contained");

			}


			final  List<Notification> pushes = PersistentPush.getStored(context);
			if(
					(TendartsSDK.instance().getAlwaysSowLastNotification() || !TendartsSDK.instance().getStackNotifications())
					&& ! showed)//always show latest
			{
				final PendingIntent pendingIntent = PersistentPush.buildPendingIntent(push, context, true);
				if( pendingIntent == null)
				{
					try
					{
						TendartsClient.instance(context).logEvent("Push","no_build_intent","" + push);
						/*
						Tracker _t = OnpublicoApplication.getPushTracker();
						if( _t != null)
						{
							_t.send(new HitBuilders.EventBuilder()
									.setNonInteraction(true)
									.setCategory("Push").setAction("no_build_intent").setLabel("" + push).build());
						}*/
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					return;
				}
				boolean makeSound = false;
				Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				if( !TendartsSDK.instance().getLimitNotificationSoundAndVibrationTime())
				{
					makeSound = true;
				}
				else if( hour >= TendartsSDK.instance().getNotificationSoundAndVibrationFirstHour() && hour < TendartsSDK.instance().getNotificationSoundAndVibrationLastHour())
				{
					makeSound = true;
				}
				Log.d(TAG, "postNotification: sound in notification:"+makeSound);

				int color = Color.parseColor("#000000");
				try
				{
					color = context.getResources().getColor(TendartsSDK.instance().getNotificationColorResource());

				}
				catch ( Exception e)
				{
					e.printStackTrace();
				}

				final  NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
						.setColor(color)
						.setSmallIcon(TendartsSDK.instance().getSmallIconResource())
						.setContentTitle(title)
						.setContentText(message)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true)
						//.setCategory(Notification.CATEGORY_SOCIAL)
						.setLargeIcon( TendartsSDK.instance().getLargeIcon(context))
						.setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
						.setPriority(android.app.Notification.PRIORITY_HIGH)
						.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
						.setExtras(extras);


				if( makeSound)
				{
					builder
							.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
							.setDefaults(android.app.Notification.DEFAULT_SOUND);
				}
				else
				{
					builder.setVibrate(new long[]{0});
				}
				final android.app.Notification notification = builder.build();

				RemoteViews rv = TendartsClient.instance(context).getCustomNotificationSmallView(push,context);
				RemoteViews rv2 = TendartsClient.instance(context).getCustomNotificationLargeView(push,context);



				if( rv != null )
				{

					_backgroundListener = new BackgroundListener(
							context,
							mNotificationManager,
							builder,
							notification,
							rv,
							rv2,
							push);
					TendartsClient.instance(context).loadBackgroundCustomNotificationData(
							_backgroundListener, push, rv, rv2, context
							);


				}
				else
				{
					Log.i(TAG, "new push notification posted" );
					if( push.hasImage(context))
					{
						final String finalMessage = message;
						final String finalTitle = title;
						final boolean finalShowed = showed;
						Communications.getImage(push.getThumbnail(), 0, new IImageDownloadObserver()
						{
							@Override
							public void onSuccess(int operationId, Bitmap data)
							{
								//builder.setLargeIcon(data)
								builder.setStyle(new
										NotificationCompat.BigPictureStyle()
										.bigPicture(data).setBigContentTitle(finalTitle)
										.setSummaryText(finalMessage));

								android.app.Notification notification1 = builder.build();


								mNotificationManager.notify(PushController.getNotificationId(push), notification1);
								notifyShowed(originalMessage, push);
								if( pushes.size() >1  && TendartsSDK.instance().getStackNotifications() && !finalShowed)
								{
									notifyList(context, mNotificationManager, push, pushes);
								}
							}

							@Override
							public void onFail(int operationId, String reason)
							{
								mNotificationManager.notify(PushController.getNotificationId(push), notification);
								notifyShowed(originalMessage, push);
								if( pushes.size() >1  && TendartsSDK.instance().getStackNotifications() && !finalShowed)
								{
									notifyList(context, mNotificationManager, push, pushes);
								}
							}
						},1024,1024);
					}
					else
					{


						mNotificationManager.notify(PushController.getNotificationId(push), notification);
						notifyShowed(originalMessage, push);

						if( pushes.size() >1  && TendartsSDK.instance().getStackNotifications() && !showed)
						{
							notifyList(context, mNotificationManager, push, pushes);
						}
					}
				}







			}



			PersistentPush.addPush(push,context);



/*
			try
			{
				NotificationsController controller = NotificationsController.instance();
				if (controller != null)
				{
					controller.load();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			*/
		}

		catch( Exception e)
		{
			e.printStackTrace();
			try
			{
				TendartsClient.instance(getApplicationContext(), getApplicationInfo()).logEvent("Push","main_exception","" + e.getMessage());
				TendartsClient.instance(getApplicationContext(),getApplicationInfo()).remoteLogException(e);

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}



	private void notifyShowed(String originalMessage, Notification push)
	{
		try
		{
			TendartsClient.instance(getApplicationContext(), getApplicationInfo()).logEvent("Push", "showed_alone", "" + originalMessage);
			TendartsClient.instance(getApplicationContext(), getApplicationInfo()).onNotificationShowed(push);


		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@NonNull
	public Notification buildPushFromBundle(Bundle extras)
	{
		String message = extras.getString("message").trim();
		String title = extras.getString("title");


		if (title == null || title.length() < 1)
		{
			int index = message.indexOf(":");
			if( index >0 && index < message.length()-2)
			{
				title = message.substring(0,index+1);
				message = message.substring(index+1);

			}
			else
			{
				//title = "Onpublico";
			}
		}
		Notification push = new Notification(title,message);
		try
		{

			for (String key : extras.keySet())
			{ //extras is the Bundle containing info
				try
				{
					String value = extras.getString(key);
					push.putExtra(key, value);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}


		/*
		String name = "img";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}

		name = "dst";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		name = "id";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		name = "code";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		name = "dsc";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		name = "dl";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		name = "sys";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		name = "sil";
		if( extras.containsKey(name))
		{
			push.putExtra(name, extras.getString(name));
		}
		*/



		//todo add payload (ctm) y parsear dsc
		return push;
	}


	/*

	private static void fillImageObserver(


			final Context context,
			final NotificationManager mNotificationManager,
			final NotificationCompat.Builder builder,
			final Notification notification,
			final RemoteViews rv,
			final RemoteViews rvBig
	)
	{
		_imageObserver = new PersistentPush.StoredPush.IImageUrlObserver()
		{
			@Override
			public void onNoImage()
			{
				Log.i(TAG, "new push notification posted no image");
				List<PersistentPush.StoredPush> pushes = PersistentPush.getStored();
				if( pushes.size()>1)
				{
					notifyList(context, mNotificationManager, null, pushes);
				}
				//always update single
				{

					mNotificationManager.notify(single_id, notification);
				}

			}

			@Override
			public void onImage( final String url, PersistentPush.StoredPush item)
			{

				Log.i(TAG, "new push notification on image");


				try
				{
					fillTarget(context,mNotificationManager,builder,notification, rv, rvBig);
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{


							Log.i(TAG, "new push notification calling picasso: "+url);
							Picasso.with(context).load(url)
									.resize(256, 256).centerCrop().into(_target); //192
						}
					});
				}
				catch (Exception e)
				{

					List<PersistentPush.StoredPush> pushes = PersistentPush.getStored();
					if (pushes.size() > 1)
					{
						notifyList(context, mNotificationManager, null, pushes);
					} //allways notify single
					{

						mNotificationManager.notify(single_id, notification);
					}
					Log.i(TAG, "error getting image:"+e.getMessage());
					e.printStackTrace();
				}

			}
		};
	}


	static void fillTarget(final Context context,
						   final NotificationManager mNotificationManager,
						   final NotificationCompat.Builder builder,
						   final Notification notification,
						   final RemoteViews rv,
						   final RemoteViews rvBig)
	{
		_target = new Target()
		{
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
			{
				Log.i(TAG, "new push notification posted with image");

				List<PersistentPush.StoredPush> pushes = PersistentPush.getStored();
				if (pushes.size() > 1)
				{
					notifyList(context, mNotificationManager, null, pushes);
				}// else
				{

					try
					{

						//notification.bigContentView = rv;
						if( rv != null && rvBig != null)
						{


							rv.setImageViewBitmap(R.id.image, bitmap);
							rv.setImageViewResource(R.id.avatar, R.mipmap.ic_op_round_red);
							builder.setContent(rv);

							rvBig.setImageViewBitmap(R.id.image, bitmap);
							rvBig.setImageViewResource(R.id.avatar,R.mipmap.ic_op_round_red);
							builder.setCustomBigContentView(rvBig);
						}
						//notification.bigContentView = rv;

						mNotificationManager.notify(single_id,
								builder.setLargeIcon(bitmap)
										.build());
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable)
			{
				Log.i(TAG, "new push notification posted no image download");
				List<PersistentPush.StoredPush> pushes = PersistentPush.getStored();
				if (pushes.size() > 1)
				{
					notifyList(context, mNotificationManager, null, pushes);
				}// else
				{

					mNotificationManager.notify(single_id, notification);
				}

			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable)
			{

				Log.i(TAG, "new push picasso prepare");
			}


		};
	}
*/

	public static void notifyList(Context context, NotificationManager mNotificationManager, Notification push, List<Notification> pushes)
	{
		try
		{
			Log.d(TAG, "notifyList");

			if( pushes.size()< 1)
			{
				return;
			}
			Intent backIntent = new Intent(context,DartsReceiver.class);

			backIntent.setAction("com.darts.SDK.OPEN_LIST");
			backIntent.putExtra("dismiss", not_id);
			backIntent.putExtra("sorg", Configuration.instance(context).getAccessToken(context).hashCode());

			//backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

			PendingIntent pi = PendingIntent.getBroadcast(context,12345,backIntent,PendingIntent.FLAG_UPDATE_CURRENT);


					/*PendingIntent.getActivities(context, PushController.NOTIFICATION_ID,
					new Intent[]{backIntent},
					PendingIntent.FLAG_UPDATE_CURRENT
			);*/

			Intent cancel = new Intent(context,DartsReceiver.class);
			cancel.setAction("com.darts.SDK.CLEAR_PUSHES");
			cancel.putExtra("dismiss", not_id);
			cancel.putExtra("sorg", Configuration.instance(context).getAccessToken(context).hashCode());


			PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, 12345, cancel, PendingIntent.FLAG_UPDATE_CURRENT);


			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
					.setColor(context.getResources().getColor(TendartsSDK.instance().getNotificationColorResource()))
					.setContentTitle(TendartsSDK.instance().getStackedNotificationTitle())
					.setContentText(TendartsSDK.instance().getStackedNotificationContent( pushes.size() ))
					.setContentIntent(pi)
					.setSmallIcon(TendartsSDK.instance().getSmallIconResource())
					//.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_push_2))

					//.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
					.setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
					.setPriority(android.app.Notification.PRIORITY_DEFAULT)
					.setVibrate(new long[]{0l})//1000, 1000, 1000, 1000, 1000})
					//.setDefaults(Notification.DEFAULT_SOUND)
					.addAction(TendartsSDK.instance().getViewStackedIconResource(), TendartsSDK.instance().getViewStackedString(), pi)
					.addAction(TendartsSDK.instance().getCancelStackedIconResource(), TendartsSDK.instance().getCancelStackedString(), pendingIntentCancel);

			NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle(builder);
			int i = 0;
			for (Notification p : pushes)
			{

				if( i==0)
				{
					i++;
					continue;
				}
				SpannableString ss = getLineTitle(p);

				inbox.addLine(ss);//p.message);
				i++;
				if (i > 5)
				{
					break;
				}
			}
			if (i < 5 && push != null)
			{
				inbox.addLine(getLineTitle(push));
			} else
			{
				inbox.setSummaryText("+" + (pushes.size() - i + 1));//todo + " " + context.getString(R.string.more));
			}
			android.app.Notification notification = inbox.build();
			// Put the auto cancel notification flag
			notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;

			mNotificationManager.notify(not_id, notification);
			try
			{
				if( push != null )
				{
					TendartsClient.instance(context).logEvent("Push", "showed_list", "" + push.getFullText());
					TendartsClient.instance(context).onNotificationShowedInList(push);
				}


			}
			catch (Exception e)
			{
				e.printStackTrace();
			}


		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{

				TendartsClient.instance(context).logEvent("Push","show_list_exc",""+e.getMessage());


			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	@NonNull
	private static SpannableString getLineTitle(Notification p)
	{
		String message = "• ";
		int end = 2;
		if( p.title != null  && p.title.length()< 15)
		{
			message = message + p.title.trim()+" ";
			end = message.length();
		}
		SpannableString ss =  new SpannableString(message+ p.message.trim());



		ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return ss;
	}


	private  class BackgroundListener implements INotifications.IBackgroundCustomNotificationLoaderListener
	{
		Context _context;
		NotificationManager _notificationManager;
		NotificationCompat.Builder _builder;
		android.app.Notification _notification;
		RemoteViews _rv;
		RemoteViews _rvBig;
		Notification _push;

		public BackgroundListener(Context context,
								  NotificationManager notificationManager,
								  NotificationCompat.Builder builder,
								  android.app.Notification notification,
								  RemoteViews rv,
								  RemoteViews rvBig,
								  Notification push)
		{
			_context = context;
			_notificationManager = notificationManager;
			_builder = builder;
			_notification = notification;
			_rv = rv;
			_rvBig = rvBig;
			_push = push;
		}

		/**
		 * Loading failed and should fallback to standard notification
		 */
		@Override
		public void revertToStandardNotification()
		{

			List<Notification> pushes = PersistentPush.getStored(getApplicationContext());

			if( !TendartsSDK.instance().getStackNotifications() || TendartsSDK.instance().getAlwaysSowLastNotification())
			{

				_notificationManager.notify(PushController.getNotificationId(_push), _notification);
			}
			if( pushes.size()>1 && TendartsSDK.instance().getStackNotifications())
			{
				notifyList(_context, _notificationManager, null, pushes);
			}
			PersistentPush.save(_context);

			_backgroundListener = null;

		}

		/**
		 * All asynchronous loading and manipulations done, remote views are ready to use
		 */
		@Override
		public void customNotificationsReady(Bitmap bitmap)
		{

			List<Notification> pushes = PersistentPush.getStored(_context);
			/*if (pushes.size() > 1)
			{
				notifyList(_context, _notificationManager, null, pushes);
			}// else*/
			{

				try
				{

					//notification.bigContentView = rv;
					if( _rv != null)
					{
						_builder.setContent(_rv);
					}
					if(_rvBig != null )
					{
						_builder.setCustomBigContentView(_rvBig);
					}
					//notification.bigContentView = rv;

					_notificationManager.notify(PushController.getNotificationId(_push),
							_builder.setLargeIcon(bitmap)
									.build());
					TendartsClient.instance(_context).onNotificationShowed(_push);

					PersistentPush.save(_context);

					if( pushes.size()>1 && TendartsSDK.instance().getStackNotifications())
					{
						notifyList(_context, _notificationManager, null, pushes);
					}

				} catch (Exception e)
				{
					e.printStackTrace();

				}
			}
			_backgroundListener = null;
		}

	}

}
