package com.tendarts.sdk.Model;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jorgearimany on 7/4/17.
 */

public class Push
{

	private static final String SERIALIZED_PUSH = "serializedPush";



	public interface IImageUrlObserver
	{
		void onNoImage();
		void onImage(String url, Push item);
	}
	static final String TAG = "STORED PUSH";
	static final String THUMBNAIL = "img";
	public Map<String,String> extras;
	public String title;
	public String message;
	public String contentId;
	public String contentType;
	public Date timestamp;
	public boolean deleted=false;
	public int notId;
	public String userData= null;

	/**
	 * To be managed by consumer.
	 */
	public boolean read;


	public String imageUrl;
	public String avatarUrl;


	Push(String title, String message)
	{
		extras = new HashMap<>();
		this.title = title;
		this.message = message;
		timestamp = new Date();
		deleted = false;
		read = false;

	}

	public Push(Intent intent)
	{
		extras = new HashMap<>();
		if( intent.hasExtra(SERIALIZED_PUSH))
		{
			try
			{

				String sjson = intent.getStringExtra(SERIALIZED_PUSH);
				deserialize(new JSONObject(sjson));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			extras = new HashMap<>();
			this.title = "";
			this.message = "";
			timestamp = new Date();
			deleted = false;
		}
	}
	public static boolean canDeserialize(Intent intent)
	{
		return intent.hasExtra(SERIALIZED_PUSH);
	}

	public void serializeToExtras(Intent intent)
	{
		String serialized = serialize().toString();
		intent.putExtra(SERIALIZED_PUSH,serialized);

	}

	public  String getFullText()
	{
		String msg = message;
		if( title != null && title.length() > 0)
		{
			msg = title+msg;
		}
		return msg;
	}

	public String getDeepUrl( ){
		return getString("dl");}

	public String getUID(){ return getString("not");}
	public String getCode()
	{
		return getString("id");
	}

	public String getImageUrl()
	{
		return imageUrl;
	}




	public void putExtra(String key, String value)
	{
		extras.put(key,value);
	}

	public String getString( String key)
	{
		return extras.get(key);
	}

	public JSONObject serialize()
	{
		try
		{
			JSONObject object = new JSONObject();
			object.put("title", title);
			object.put("message", message);
			if( timestamp != null)
			{
				object.put("timestamp", timestamp.getTime());
			}
			if( imageUrl != null)
			{
				object.put("imageUrl", imageUrl);
			}
			if( avatarUrl != null)
			{
				object.put("avatarUrl",avatarUrl);
			}
			JSONArray array = new JSONArray();
			for( String key : extras.keySet())
			{
				JSONObject item = new JSONObject();
				item.put("key", key);
				Object o = extras.get(key);
				if( o == null)
				{
					o ="";
				}
				item.put("value",o.toString()); // extras.get(key));
				array.put(item);
			}
			object.put("extras", array);

			object.put("contentId", contentId);
			object.put("contentType", contentType);
			object.put("notId", notId);
			object.put("deleted",deleted);
			object.put("userData",userData);

			//Log.i(TAG, "serialized push:"+object);
			return object;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;

	}
	public void deserialize(JSONObject object)
	{
		try
		{
			Log.i(TAG, "Deserialize: " + object);
			if(object.isNull("title"))
			{
				title = null;
			}
			else
			{
				title = object.getString("title");
			}
			message = object.getString("message");

			JSONArray array = object.getJSONArray("extras");
			extras = new HashMap<>();
			for( int i = 0; i < array.length(); i++)
			{
				JSONObject item = array.getJSONObject(i);
				//Log.i(TAG, "deserializing extras:"+item);
				try
				{
					extras.put(item.getString("key"), item.getString("value"));
				}
				catch (Exception e)
				{

						e.printStackTrace();

				}

			}
			if( !object.isNull("imageUrl"))
			{
				imageUrl = object.getString("imageUrl");
			}
			else
			{
				imageUrl = null;
			}
			if( !object.isNull("avatarUrl"))
			{
				avatarUrl= object.getString("avatarUrl");
			}

			if( !object.isNull("timestamp"))
			{
				long ts = object.getLong("timestamp");
				timestamp = new Date(ts);
			}

			if( !object.isNull("contentId"))
			{
				contentId=object.getString("contentId");
			}
			if( !object.isNull("contentType"))
			{
				contentType=object.getString("contentType");
			}
			if( !object.isNull("notId"))
			{
				notId=object.getInt("notId");
			}
			if( !object.isNull("deleted"))
			{
				deleted=object.getBoolean("deleted");
			}
			if( !object.isNull("userData"))
			{
				userData = object.getString(userData);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String toString()
	{
		JSONObject obj = serialize();
		if( obj != null)
		{
			return obj.toString();
		}
		return "null";
	}


}
