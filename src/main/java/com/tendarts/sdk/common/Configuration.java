package com.tendarts.sdk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by jorgearimany on 3/4/17.
 */

public class Configuration implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private static final String PRIVATE_PREFS = "private";
	private static final String TAG = "SDK:Config";

	private final static String PUSH = "10darts_sdk_push";
	private final static String PUSH_CODE = "sdk_push_code";
	private final static String LAST_CITY = "sdk_last_city";
	private final static String PUSH_RETRY_MILLISECONDS = "sdk_pushRetryMillieconds";
	private final static String PUSH_SENT_TOKEN = "sdk_pushSentToken";
	private final static String PUSH_USER = "sdk_pushUser";
	private final static String PUSH_RETRY_TOKEN = "sdk_pushRetryToken";
	private final static String INSTALL_SOURCE = "sdk_installSource";
	private final static String NOT_ENABLED = "sdk_notifications_enabled";
	private final static String LAST_BADGE = "sdk_lastBadge";
	private final static String USER_CODE = "sdk_user";
	private final static String ACCESS_TOKEN = "sdk_accessToken";
	private final static String LAST_GEOSTATS_SENT = "sdk_last_geostats";
	private final static String PENDING_COMMUNICATIONS = "sdk_pending_communications";
	private final static String PENDING_TOKEN = "sdk_pending_token";
	private final static String TOKEN_RETRIES = "sdk_token_retries";
	private final static String PENDING_LINK = "sdk_pending_link";


	private final static String S_GCM_SENDER_ID		="sdk_gcm_sender_id";
	private final static String S_CLIENT_CLASS		="sdk_client_class";
	private final static String	S_ACCESS_TOKEN 		="sdk_access_token";


	private  final static String TESTING = "sdk_testing";
	public static final String DELAYED_PREFIX = "delayed_";


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
	private long _lastGeostatsSent;

	private String appId= null;
	private boolean inSoftMode = false;

	static WeakReference<Context> _context = null;


	public static Context getWeakContext()
	{
		if( _context == null)
		{
			return null;
		}
		return _context.get();
	}

	public static Configuration instance(Context c)
	{
		//Log.d(TAG, "instance: "+c.getPackageName()+" app "+ c.getApplicationContext().getPackageName());
		if (_me == null)
		{
			_me = new Configuration(c.getApplicationContext());
		}
		return _me;
	}

	//----------------------------------------------------------------------------------------------
	//              CONSTRUCTOR
	//----------------------------------------------------------------------------------------------
	private Configuration(Context c)
	{


		try
		{

			if (c != null)
			{
				_context = new WeakReference<Context>(c.getApplicationContext());

				String appId = getApppId(c);
				if(appId != null)
				{
					Log.d(TAG, "Configuration:  in soft mode:"+appId);
					inSoftMode = true;
					_settings = c.getSharedPreferences(appId, Context.MODE_PRIVATE);

				}
				else
				{
					inSoftMode = false;
					_settings = c.getSharedPreferences(PRIVATE_PREFS, Context.MODE_PRIVATE);
				}



				_settings = c.getSharedPreferences(PRIVATE_PREFS, Context.MODE_PRIVATE);
				_settings.registerOnSharedPreferenceChangeListener(this);


				_push = _settings.getString(PUSH, null);
				_push_code = _settings.getString(PUSH_CODE, null);

				_lastCity = _settings.getString(LAST_CITY, null);


				_pushRetryMilliseconds = _settings.getLong(PUSH_RETRY_MILLISECONDS, 0);
				_lastGeostatsSent = _settings.getLong(LAST_GEOSTATS_SENT, 0);
				_lastGeostatsSent = _settings.getLong(LAST_GEOSTATS_SENT, 0);
				_pushSentToken = _settings.getString(PUSH_SENT_TOKEN, null);
				_pushUser = _settings.getString(PUSH_USER, null);
				_pushRetryToken = _settings.getString(PUSH_RETRY_TOKEN, null);
				_installSource = _settings.getString(INSTALL_SOURCE, null);
				_notificationsEnabled = _settings.getBoolean(NOT_ENABLED, true);
				_lastBadge = _settings.getInt(LAST_BADGE, 0);

				_userCode = _settings.getString(USER_CODE, null);
				_accessToken = _settings.getString(ACCESS_TOKEN, null);


			}
		} catch (Exception e)
		{
			Log.e(TAG, "Could not initiate configuration!!" + e.getMessage());
		}

	}



	public String getTest()
	{
		return _settings.getString(TESTING,null);
	}

	public void setTest( String test)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(TESTING, test);
			editor.apply();
		}
		catch (Exception e)
		{
			Log.e(TAG,"could not save test: "+e.getMessage());
		}

	}




	//----------------------------------------------------------------------------------------------
	//              GETTERS
	//----------------------------------------------------------------------------------------------

	public String getPush()
	{
		return _push;
	}

	public String getPushCode()
	{
		return _push_code;
	}

	public String getLastCity()
	{
		return _lastCity;
	}

	public long getPushRetryMilliseconds()
	{
		return _pushRetryMilliseconds;
	}

	public String getPushSentToken()
	{
		return _pushSentToken;
	}

	public String getPushUser()
	{
		return _pushUser;
	}

	public String getPushRetryToken()
	{
		return _pushRetryToken;
	}

	public String getInstallSource()
	{
		return _installSource;
	}

	public boolean getNotificationsEnabled()
	{
		return _notificationsEnabled;
	}

	;

	public int getLastBadge()
	{
		return _lastBadge;
	}

	private String getAccessToken()
	{
		return _accessToken;
	}

	public String getPendingCommunications()
	{
		return _settings.getString(PENDING_COMMUNICATIONS, "");
	}

	public int getTokenRetries()
	{
		return _settings.getInt(TOKEN_RETRIES, 0);
	}

	public String getPendingToken()
	{
		return _settings.getString(PENDING_TOKEN, null);
	}

	public String getPendingLink()
	{
		return _settings.getString(PENDING_LINK,null);
	}


	/**
	 * USER CODE == PERSONA.RESOURCE_URI
	 *
	 * @return
	 */
	public String getUserCode()
	{
		return _userCode;
	}


	public String getGCMDefaultSenderId(Context context, String packageName)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String id = bundle.getString("gcm_defaultSenderId");
			// TODO: this removes the last 0, but the fix should be other
			id = id.substring(0, id.length()-1);
			Log.d(TAG, "gcm_defaultSenderId from " + ai + ": " + id);
			Util.printExtras(TAG, bundle);

			if( id == null || id.length() < 1)
			{
				if( inSoftMode)
				{
					return getSoftGCMDefaultSenderId();
				}
			}

			return id;
		} catch (Exception e)
		{
			if( inSoftMode)
			{
				return getSoftGCMDefaultSenderId();
			}

			Log.e(TAG, "Please add  gcm_sender_id in manifestPlaceholders" + e);
		}
		return null;
	}




	private String getGCMDefaultSenderId(Context context)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;


			String id = "";

			Object o = bundle.get("gcm_defaultSenderId");
			if (o == null)
			{
				if( o == null )
				{
					if( inSoftMode)
					{
						return getSoftGCMDefaultSenderId();
					}
				}
				Log.e(TAG, "Please add  gcm_sender_id in manifestPlaceholders");
				return null;
			}

			if (!o.getClass().isAssignableFrom(String.class))
			{
				float f = (float) o;
				long num = (long) f;
				id = "" + num;
			} else
			{
				id = (String) o;
			}

			Log.d(TAG, "getGCMDefaultSenderId: " + id);
			return id;
		} catch (Exception e)
		{
			if( inSoftMode)
				{
					return getSoftGCMDefaultSenderId();
				}
			Log.e(TAG, "Please add  gcm_sender_id in manifestPlaceholders");
		}
		return null;
	}

	private String getSoftGCMDefaultSenderId()
	{
		return _settings.getString(S_GCM_SENDER_ID,null);
	}

	private String getSoftClientClassName()
	{
		String className = _settings.getString(S_CLIENT_CLASS, null);
		Log.d(TAG, "getSoftClientClassName: "+className);
		return  className;
	}

	private String getSoftAccessToken()
	{
		return _settings.getString(S_ACCESS_TOKEN,null);
	}

	public static String getClientClassName(Context context, ApplicationInfo applicationInfo)
	{
		ApplicationInfo info = null;
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
		if (id == null|| id.length() < 1)
		{

				if( Configuration.instance(context).inSoftMode)
				{
					Log.d(TAG, "getClientClassName: in soft mode, getting from config");
					return Configuration.instance(context).getSoftClientClassName();
				}


			Log.e("SDK:Config", "Please add  tendarts_sdk_client_class:\"com.yourcompany.YourClientClass\" in manifestPlaceholders");
			Log.d(TAG, "not found sdk_clientClass in :" + info.packageName);
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
			if (id == null|| id.length() < 1)
			{
					if( Configuration.instance(context).inSoftMode)
					{
						Log.d(TAG, "getClientClassName:  in soft mode");
						return Configuration.instance(context).getSoftClientClassName();
					}

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




	public static String getAccessToken(Context context)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String id = bundle.getString("sdk_accessToken");
			if (id != null)
			{
				if( id. startsWith(DELAYED_PREFIX))
				{
					String app = id.substring(DELAYED_PREFIX.length());
					if( app == null ||app.length()<1)
					{
						return null;
					}
					instance(context).appId = app;
					instance(context).inSoftMode = true;

					return Configuration.instance(context).getSoftAccessToken();

				}
				String token = Configuration.instance(context).getAccessToken();
				if (!id.equals(token))
				{
					Configuration.instance(context).setAccessToken(id);
				}
			} else
			{
				id = Configuration.instance(context).getAccessToken();
			}
			if (id == null || id.length() < 1)
			{

					if( Configuration.instance(context).inSoftMode)
					{
						return Configuration.instance(context).getSoftAccessToken();
					}

				Log.w(TAG, "Please add  tendarts_sdk_access_token:\"YOUR ACCESS TOKEN\" in manifestPlaceholders");
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


	public void setPush(String push)
	{
		_push = push;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH, push);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save push: " + e.getMessage());
		}

	}

	public void setAccessToken(String token)
	{
		_accessToken = token;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(ACCESS_TOKEN, token);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save access token: " + e.getMessage());
		}

	}


	public void setPushCode(String push_code)
	{
		_push_code = push_code;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_CODE, push_code);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save push code: " + e.getMessage());
		}

	}

	public void setLastCity(String city)
	{
		_lastCity = city;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(LAST_CITY, city);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save last city: " + e.getMessage());
		}

	}


	public void setPushRetryMillieconds(long value)
	{
		_pushRetryMilliseconds = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putLong(PUSH_RETRY_MILLISECONDS, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}

	}


	public void setPushSentToken(String value)
	{
		_pushSentToken = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_SENT_TOKEN, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}

	}

	public void setPushUser(String value)
	{
		_pushUser = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_USER, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}

	}

	public void setPushRetryToken(String value)
	{
		_pushRetryToken = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PUSH_RETRY_TOKEN, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}

	}

	public void setInstallSource(String source)
	{
		_installSource = source;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(INSTALL_SOURCE, source);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save INSTALL SOURCE: " + e.getMessage());
		}
	}


	public void setSoftGCMDefaultSenderId( String value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(S_GCM_SENDER_ID, value);
			editor.commit();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save INSTALL SOURCE: " + e.getMessage());
		}
	}

	public void setSoftClientClassName( String value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(S_CLIENT_CLASS, value);
			editor.commit();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save INSTALL SOURCE: " + e.getMessage());
		}
	}

	public void setSoftAccessToken( String value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(S_ACCESS_TOKEN, value);
			editor.commit();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save INSTALL SOURCE: " + e.getMessage());
		}
	}



	/**
	 * USER CODE == PERSONA.RESOURCE_URI
	 *
	 * @param code U PERSONA.RESOURCE_URI
	 */
	public void setUserCode(String code)
	{
		_userCode = code;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(USER_CODE, code);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save USER CODE	: " + e.getMessage());
		}
	}

	public void setNotificationsEnabled(boolean enabled)
	{
		_notificationsEnabled = enabled;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putBoolean(NOT_ENABLED, enabled);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save notifications enabled: " + e.getMessage());
		}
	}


	public void setLastBadge(int value)
	{
		_lastBadge = value;
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putInt(LAST_BADGE, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}

	}

	public void setPendingCommunications(String value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PENDING_COMMUNICATIONS, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}


	}


	public void setPendingLink(String value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PENDING_LINK, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}
	}

	public void setPendingToken(String value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(PENDING_TOKEN, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}
	}

	public void setTokenRetries(int value)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putInt(TOKEN_RETRIES, value);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}
	}


	//----------------------------------------------------------------------------------------------
	//          					    EVENTS
	//----------------------------------------------------------------------------------------------

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
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
			case LAST_GEOSTATS_SENT:
				_lastGeostatsSent = sharedPreferences.getLong(key, _lastGeostatsSent);
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
				_installSource = sharedPreferences.getString(key, _installSource);
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
	public String loadPrivate(String key)
	{
		if (key == null)
		{
			return null;
		}
		try
		{
			return _settings.getString(key, null);
		} catch (Exception e)
		{
			Log.e(TAG, "could not load for key " + key + ": " + e.getMessage());
			return null;
		}
	}

	public void savePrivate(String key, String data)
	{
		try
		{
			SharedPreferences.Editor editor = _settings.edit();
			editor.putString(key, data);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save private key: " + key + ": " + e.getMessage());
		}
	}

	public static boolean shouldSendGeostats(Context context)
	{

		long last = instance(context)._lastGeostatsSent;
		long now = new Date().getTime();
		long elapsed = (now - last);
		boolean rv = elapsed > 120000;
		Log.d(TAG, "shouldSendGeostats: " + rv + " e:" + elapsed);
		return rv;

	}

	public static void notifyGeostatsSent(Context context)
	{
		Configuration c = instance(context);
		c._lastGeostatsSent = new Date().getTime();
		try
		{
			SharedPreferences.Editor editor = c._settings.edit();
			editor.putLong(LAST_GEOSTATS_SENT, c._lastGeostatsSent);
			editor.apply();
		} catch (Exception e)
		{
			Log.e(TAG, "could not save : " + e.getMessage());
		}
	}



	private String getApppId(Context context)
	{
		try
		{
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String id = bundle.getString("sdk_accessToken");
			if (id != null)
			{
				if (id.startsWith(DELAYED_PREFIX))
				{
					String app = id.substring(DELAYED_PREFIX.length());
					if (app == null || app.length() < 1)
					{
						return null;
					}
					appId = app;
					inSoftMode = true;
					return app;

				}
			}

		} catch (Exception e)
		{
			Log.d(TAG, "default mode");
			return null;

		}
		return null;
	}

}


