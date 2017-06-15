package com.darts.sdk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.health.ServiceHealthStats;
import android.util.Log;

/**
 * Created by jorgearimany on 3/4/17.
 */

public class Configuration implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private static final String PRIVATE_PREFS = "private";
	private static final String TAG = "SDK:Config";

	private final static String PUSH = "sdk_push";
	private final static String PUSH_CODE ="sdk_push_code";
	private final static String LAST_CITY = "sdk_last_city";
	private final static String PUSH_RETRY_MILLISECONDS = "sdk_pushRetryMillieconds";
	private final static String PUSH_SENT_TOKEN 	="sdk_pushSentToken";
	private final static String PUSH_USER 			="sdk_pushUser";
	private final static String PUSH_RETRY_TOKEN 	="sdk_pushRetryToken";
	private final static String INSTALL_SOURCE	="sdk_installSource";
	private final static String NOT_ENABLED = "sdk_notifications_enabled";
	private final static String LAST_BADGE 			="sdk_lastBadge";
	private final static String USER_CODE 			="sdk_user";
	private final static String ACCESS_TOKEN		="sdk_accessToken";


	private static Configuration _me;

	private SharedPreferences _settings;

	private String _push;
	private String _push_code;
	private String _lastCity;
	private long _pushRetryMilliseconds;
	private String _pushSentToken;
	private String _pushUser;
	private String _pushRetryToken;
	private String _installSource;
	private boolean _notificationsEnabled = true;
	private int _lastBadge;
	private String _userCode;
	private String _accessToken = null;

	private static Configuration instance()
	{
		if( _me == null)
		{
			return new Configuration(null);
		}
		return _me;
	}
	public static Configuration instance(Context c)
	{
		if( _me == null)
		{
			_me = new Configuration(c);
		}
		return _me;
	}

	//----------------------------------------------------------------------------------------------
	//              CONSTRUCTOR
	//----------------------------------------------------------------------------------------------
	private  Configuration ( Context c)
	{


		try
		{


			if( c != null)
			{
				_settings = c.getSharedPreferences(PRIVATE_PREFS, Context.MODE_PRIVATE);
				_settings.registerOnSharedPreferenceChangeListener(this);



				_push = _settings.getString(PUSH, null);
				_push_code = _settings.getString(PUSH_CODE, null);

				_lastCity = _settings.getString(LAST_CITY, null);



				_pushRetryMilliseconds = _settings.getLong(PUSH_RETRY_MILLISECONDS, 0);
				_pushSentToken = _settings.getString(PUSH_SENT_TOKEN, null);
				_pushUser = _settings.getString(PUSH_USER, null);
				_pushRetryToken = _settings.getString(PUSH_RETRY_TOKEN, null);
				_installSource = _settings.getString(INSTALL_SOURCE, null);
				_notificationsEnabled = _settings.getBoolean(NOT_ENABLED, true);
				_lastBadge 				= _settings.getInt(LAST_BADGE, 0);

				_userCode = _settings.getString(USER_CODE, null);
				_accessToken = _settings.getString(ACCESS_TOKEN,null);


			}
		}
		catch (Exception e)
		{
			Log.e(TAG,"Could not initiate configuration!!" + e.getMessage());
		}

	}


	//----------------------------------------------------------------------------------------------
	//              GETTERS
	//----------------------------------------------------------------------------------------------

	public String getPush() { return _push;	}
	public String getPushCode(){return _push_code;}
	public String getLastCity(){ return _lastCity;}
	public long getPushRetryMilliseconds(){return _pushRetryMilliseconds;}
	public String getPushSentToken(){return _pushSentToken;}
	public String getPushUser(){return  _pushUser;}
	public String getPushRetryToken(){return _pushRetryToken;}
	public String getInstallSource(){ return _installSource;}
	public boolean getNotificationsEnabled(){return _notificationsEnabled;};
	public int 	  getLastBadge(){return _lastBadge;}
	public String getAccessToken(){
		return _accessToken;
	}

	/**
	 * USER CODE == PERSONA.RESOURCE_URI
	 * @return
	 */
	public String getUserCode(){return _userCode;}


	public String getGCMDefaultSenderId(Context context, String packageName)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String id = bundle.getString("gcm_defaultSenderId");
			Log.d(TAG, "gcm_defaultSenderId from "+ai+": "+id);
			Util.printExtras(TAG,bundle);
			return id;
		} catch (Exception e)
		{
			Log.e(TAG, "Please add  gcm_sender_id in manifestPlaceholders"+ e);
		}
		return null;
	}



	public String getGCMDefaultSenderId(Context context)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;


			String id = "";

			Object o = bundle.get("gcm_defaultSenderId");
			if( o == null)
			{
				Log.e(TAG, "Please add  gcm_sender_id in manifestPlaceholders");
				return null;
			}

			if( !o.getClass().isAssignableFrom(String.class))
			{
				float f = (float)o;
				long num = (long) f;
				id = ""+num;
			}
			else
			{
				id = (String)o;
			}

			Log.d(TAG, "getGCMDefaultSenderId: "+id);
			return id;
		} catch (Exception e)
		{
			Log.e(TAG, "Please add  gcm_sender_id in manifestPlaceholders");
		}
		return null;
	}


	public static String getClientClassName(Context context, ApplicationInfo applicationInfo)
	{
		ApplicationInfo info = null ;
		try
		{
			info = context.getPackageManager().getApplicationInfo(applicationInfo.packageName, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
			return getClientClassName(context);
		}
		Bundle bundle = info.metaData;
		String id = bundle.getString("sdk_clientClass");
		if( id == null)
		{
			Log.e("SDK:Config", "Please add  tendarts_sdk_client_class:\"com.yourcompany.YourClientClass\" in manifestPlaceholders");
			Log.d(TAG, "not found sdk_clientClass in :"+info.packageName);
			Util.printExtras(TAG, bundle);
		}
		return id;
	}
	public static String getClientClassName(Context context)
	{
		try
		{


			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String id = bundle.getString("sdk_clientClass");
			if( id == null)
			{
				Log.e("SDK:Config", "Please add  tendarts_sdk_client_class:\"com.yourcompany.YourClientClass\" in manifestPlaceholders");

				Log.d(TAG, "getClientClassName: getClientClass not found for sdk_clientClass in :");
				Util.printExtras(TAG, bundle);
			}
			return id;
		} catch (Exception e)
		{
			Log.w(TAG, "Please add  tendarts_sdk_client_class:\\\"com.yourcompany.YourClientClass\\\" in manifestPlaceholders");
			return null;

		}
	}

	public String getAccessToken(Context context)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String id = bundle.getString("darts_accessToken");
			if( id != null)
			{
				String token = Configuration.instance(context).getAccessToken();
				if( !id.equals(token))
				{
					Configuration.instance(context).setAccessToken(id);
				}
			}
			else
			{
				id = Configuration.instance(context).getAccessToken();
			}
			return id;
		} catch (Exception e)
		{
			Log.w(TAG, "Please add  tendarts_sdk_access_token:\"YOUR ACCESS TOKEN\" in manifestPlaceholders");
			return null;

		}
	}

	//----------------------------------------------------------------------------------------------
	//              SETTERS
	//----------------------------------------------------------------------------------------------


	public void setPush( String push)
	{
		_push = push;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH, push);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save push: "+e.getMessage());
		}

	}

	public void setAccessToken( String token)
	{
		_accessToken = token;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(ACCESS_TOKEN, token);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save access token: "+e.getMessage());
		}

	}


	public void setPushCode( String push_code)
	{
		_push_code = push_code;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_CODE, push_code);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save push code: "+e.getMessage());
		}

	}
	public void setLastCity( String city)
	{
		_lastCity = city;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(LAST_CITY,city);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save last city: "+e.getMessage());
		}

	}


	public void setPushRetryMillieconds( long value)
	{
		_pushRetryMilliseconds = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putLong(PUSH_RETRY_MILLISECONDS, value);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save : "+e.getMessage());
		}

	}


	public void setPushSentToken( String value)
	{
		_pushSentToken = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_SENT_TOKEN, value);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save : "+e.getMessage());
		}

	}
	public void setPushUser( String value)
	{
		_pushUser= value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_USER,value);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save : "+e.getMessage());
		}

	}

	public void setPushRetryToken( String value)
	{
		_pushRetryToken= value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_RETRY_TOKEN,value);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save : "+e.getMessage());
		}

	}

	public void setInstallSource( String source)
	{
		_installSource = source;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(INSTALL_SOURCE	,source);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save INSTALL SOURCE: "+e.getMessage());
		}
	}


	/**
	 * USER CODE == PERSONA.RESOURCE_URI
	 * @param code U PERSONA.RESOURCE_URI
	 */
	public void setUserCode( String code)
	{
		_userCode = code;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(USER_CODE	,code);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save USER CODE	: "+e.getMessage());
		}
	}

	public void setNotificationsEnabled( boolean enabled)
	{
		_notificationsEnabled = enabled;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putBoolean(NOT_ENABLED, enabled);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save notifications enabled: "+e.getMessage());
		}
	}


	public void setLastBadge( int value)
	{
		_lastBadge= value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putInt(LAST_BADGE,value);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save : "+e.getMessage());
		}

	}

	//----------------------------------------------------------------------------------------------
	//          					    EVENTS
	//----------------------------------------------------------------------------------------------

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key)
		{

			case PUSH:
				_push = sharedPreferences.getString(key, _push);
				break;
			case PUSH_CODE:
				_push_code = sharedPreferences.getString(key, _push_code);
				break;
			case LAST_CITY:
				_lastCity = sharedPreferences.getString(key, _lastCity);
				break;

			case PUSH_RETRY_MILLISECONDS:
				_pushRetryMilliseconds = sharedPreferences.getLong(key, _pushRetryMilliseconds);
				break;
			case PUSH_SENT_TOKEN:
				_pushSentToken = sharedPreferences.getString(key, _pushSentToken);
				break;
			case PUSH_USER:
				_pushUser = sharedPreferences.getString(key, _pushUser);
				break;
			case PUSH_RETRY_TOKEN:
				_pushRetryToken = sharedPreferences.getString(key, _pushRetryToken);
				break;
			case INSTALL_SOURCE:
				_installSource = sharedPreferences.getString(key,_installSource);
				break;
			case NOT_ENABLED:
				_notificationsEnabled = sharedPreferences.getBoolean(key, _notificationsEnabled);
				break;
			case LAST_BADGE:
				_lastBadge = sharedPreferences.getInt(key, _lastBadge);
				break;
			case USER_CODE:
				_userCode = sharedPreferences.getString(key, _userCode);
				break;
			case ACCESS_TOKEN:
				_accessToken = sharedPreferences.getString(key, _accessToken);
				break;

		}
	}


	//----------------------------------------------------------------------------------------------
	//              			HELPERS
	//----------------------------------------------------------------------------------------------
	public  String loadPrivate( String key )
	{
		if( key == null )
		{
			return null;
		}
		try
		{
			return _settings.getString(key, null);
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not load for key "+key+": "+e.getMessage());
			return  null    ;
		}
	}

	public void savePrivate( String key,String data)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(key,data);
			editor.commit();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save private key: "+key +": "+e.getMessage());
		}
	}

}
