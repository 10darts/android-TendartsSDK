package com.tendarts.sdk.Model;

import android.content.Context;
import android.content.Intent;

import com.tendarts.sdk.client.TendartsClient;

/**
 * Created by jorgearimany on 8/4/17.
 */

public class Notification extends Push
{
	public Notification(String title, String message)
	{
		super(title, message);
	}

	public Notification(Intent intent)
	{
		super(intent);
	}

	public static boolean canDeserialize( Intent intent)
	{
		return  Push.canDeserialize(intent);
	}
	public boolean hasImage(Context context)
	{
		String thumb = getString(THUMBNAIL);
		if( thumb != null && thumb.length()>3)
		{
			return true;
		}

		return TendartsClient.instance(context).notificationHasImage( this);
	}
	public String getThumbnail()
	{
		return getString(THUMBNAIL);
	}

//todo --> migrate to onpublico

	/*
	public boolean isAuthor()
	{
		String dst = getString("dst");
		if( dst != null)
		{
			String code = getString("code");
			if( code == null)
			{
				return false;
			}
			switch (dst)
			{
				case "usr":
					return true;

				default:
					return false;
			}
		}
		return false;
	}
	public void loadImageUrl(final IImageUrlObserver observer, boolean large)

	{
		if( observer != null)
		{
			if( imageUrl != null && large && imageUrl.contains("94x94") && !isAuthor()&& hasImage())
			{
				imageUrl = null	;
			}

			if( imageUrl != null)
			{
				observer.onImage(imageUrl, this);
				return;
			}

			if(hasImage())
			{

				String thumbnail = getString(THUMBNAIL);
				if( thumbnail != null && thumbnail.length() > 0)
				{
					if( large &&( !thumbnail.contains("94x94") || isAuthor()) )
					{
						Log.d(TAG, "loadImageUrl: has th thumbnail");
						imageUrl = thumbnail;
						observer.onImage(imageUrl, this);
						return;
					}
				}

				String dst = getString("dst");
				String code = getString("code");

				Log.i(TAG,"loading image url "+dst+":"+code);
				if( dst.equals( "usr"))
				{
					AuthorsController.instance().loadWithObserver(code, new BaseController.ISingleObserver()
					{
						@Override
						public void onSuccess(Object item)
						{
							try
							{
								Log.i(TAG, "onAuthor:"+item);
								Author author = (Author) item;
								if( author != null)
								{
									imageUrl = author.avatarThumbnail;
									observer.onImage(imageUrl, Push.this);
									save();
								}

							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}

						@Override
						public void onError(String error)
						{
							Log.w(TAG, "eror getting author:"+error);
							observer.onNoImage();
						}
					});
					return;
				}
				if( dst.equals( "n"))
				{

					NewsController.instance().loadWithObserver(code, new BaseController.ISingleObserver()
					{
						@Override
						public void onSuccess(Object item)
						{
							try
							{
								Log.i(TAG, "onNews:"+item);
								News news = (News) item;
								if (news != null)
								{
									if( news.author != null && news.author.isTracked)
									{
										avatarUrl = news.author.avatarThumbnail;
									}
									imageUrl = news.mediaList.get(0).thumbnail;

									observer.onImage(imageUrl, Push.this);
									save();
								}

							} catch (Exception e)
							{
								e.printStackTrace();
							}
						}

						@Override
						public void onError(String error)
						{
							Log.w(TAG, "eror getting news:" + error);
							observer.onNoImage();
						}
					});
					return;
				}
				if( dst.equals( "evt"))
				{
					EventsController.instance().loadWithObserver(code, new BaseController.ISingleObserver()
					{
						@Override
						public void onSuccess(Object item)
						{
							try
							{
								Log.i(TAG, "onEvent:"+item);
								Event event = (Event) item;
								if( event != null)
								{
									if( event.author!=null && event.author.isTracked)
									{
										avatarUrl = event.author.avatarThumbnail;
									}
									if( event.thumbnails.size() > 0)
									{
										imageUrl = event.thumbnails.get(0).thumbnail;
									}
									else
									{
										imageUrl= event.mediaList.get(0).thumbnail;
									}
									observer.onImage(imageUrl, Push.this);
									save();
								}

							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}

						@Override
						public void onError(String error)
						{
							Log.w(TAG, "eror getting event:"+error);
							observer.onNoImage();
						}
					});
				}
				return;
			}
			else
			{
				observer.onNoImage();
			}
		}

	}*/
}
