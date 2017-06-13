package com.darts.sdk.Model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.darts.sdk.client.DartsClient;
import com.darts.sdk.common.Configuration;
import com.darts.sdk.common.PushController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by jorgearimany on 6/4/17.
 */

//todo: no quitar inmediatamente, marcar como leido

public class PersistentPush
{
	private static final String TAG = "Persistent Push";




	static ArrayList<Notification> _list = new ArrayList<>();


	/**
	 * mark the push as deleted
	 * @param id
	 * @param context
	 */
	public static void removeById(String id, Context context)
	{

		Log.i(TAG,"Remove id:"+id);
		if( id == null || id.isEmpty())
		{
			return;
		}
		getStored(context);
		synchronized (_list)
		{
			for (int i = 0; i < _list.size(); i++)
			{
				Notification push = _list.get(i);
				String pid= push.getString("id");
				Log.d(TAG,"checking id "+pid);
				if( id.equals(pid))
				{
					Log.d(TAG, "match, removing");
					push.deleted = true; //_list.remove(i);
					JSONArray toSave = saveToJson(_list);
					Configuration.instance(context).savePrivate(PUSH_KEY, toSave.toString());
					return	;
				}
			}
		}
	}


	public static PendingIntent buildPendingIntent(Notification push, Context context, boolean single)
	{
		return  buildPendingIntent( push,  context ,
				Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK, single);
	}
	public static PendingIntent buildPendingIntent(Notification push, Context context, int flags, boolean single)
	{

		Intent intent = buildIntent(push, context, flags);
		if (intent == null) return null;

//		intent.putExtras((Bundle)extras.clone());

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				single? PushController.getNotificationId(push):
						PushController.NOTIFICATION_ID,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT);


/*

		PendingIntent pendingIntent = PendingIntent.getActivities(context,
				single? PushController.SINGLE_ID:
						PushController.NOTIFICATION_ID,
				new Intent[]{intent},
				PendingIntent.FLAG_UPDATE_CURRENT
		);*/


		return pendingIntent;

	}

	public static boolean isDestinationRootWindow(String  dst)
	{
		if (dst != null)
		{
			switch (dst)
			{


				case "hot":
				case "tag":
				case "trks":
				case "trn":
					return true;
			}

		}
		return false;
	}
	public static boolean isDestinationRootWindow(Notification push)
	{
		if( push != null)
		{
			String dst = push.getString("dst");
			return isDestinationRootWindow(dst);
		}
		return false;
	}

	@Nullable
	public static Intent buildIntent(Notification push, Context context, int flags)
	{


		Intent intent = buildIntent(context, flags, push);
		if (intent == null) return null;
		return intent;
	}

	@Nullable
	public static Intent buildIntent(Context context, int flags,Notification push)
	{

		Intent backIntent = new Intent();

		backIntent.setAction("com.darts.sdk.OPEN_PUSH");
		// backIntent.putExtra("dismiss", not_id);
		backIntent.putExtra("sorg", Configuration.instance(context).getAccessToken(context).hashCode());
		push.serializeToExtras(backIntent);
		backIntent.addFlags(flags);

		return backIntent;
	}


	private final static String PUSH_KEY ="sdk_stored_pushes";

	public static void addPush(Notification push, Context context)
	{
		//getStored(context);
		synchronized (_list)
		{
			//ArrayList<Notification> list = getStored(context);



				for (int i = 0; i < _list.size(); i++)
				{
					Notification p = _list.get(i);
					String pid= p.getUID();
					String id = push.getUID();
					if( id != null &&  id.equals(pid))
					{
						Log.d(TAG, "adding existing");
						DartsClient.instance(context).logEvent("PUSH","duplicate push received, ignoring",id);
						//do nothing, adding existing item
						return	;
					}
				}

			//list.add(push);
			_list.add(push);

			//_list = list;
		}
		save(context);
	}
	public static void save(Context context)
	{
		try
		{
			removeDeleted(context);
			synchronized (_list)
			{

				JSONArray toSave = saveToJson(_list);
				Log.d(TAG, "save: "+toSave);
				Configuration.instance(context).savePrivate(PUSH_KEY, toSave.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void removeDeleted(Context context)
	{

		synchronized (_list)
		{
			ArrayList<Push> to_delete = new ArrayList<>();
			for (int i = 0; i < _list.size(); i++)
			{

				Notification p = _list.get(i);

				long elapsed = new Date().getTime()-p.timestamp.getTime();


				if( p.deleted && elapsed > (48*60*60*1000))
				{
					to_delete.add(p);
				}
			}
			Log.d(TAG, "removeDeleted past 48 h"+to_delete);
			_list.removeAll(to_delete);
			//save ( context);

		}
	}

	public static void clear(Context context)
	{
		getStored(context);
		synchronized (_list)
		{
			for (int i = 0; i < _list.size(); i++)
			{
				Notification p = _list.get(i);
				p.deleted = true;
			}
		}
		save(context);

	}

	public static ArrayList<Notification> getStored(Context context)
	{
		try
		{
			String json = Configuration.instance(context).loadPrivate(PUSH_KEY);
			if( json != null)
			{
				if( json.isEmpty())
				{
					_list = new ArrayList<>();
				}
				else
				{
					JSONArray array = new JSONArray(json);
					synchronized (_list)
					{
						_list = loadFromJson(array);
					}
				}

				ArrayList<Notification> toReturn = new ArrayList<>();
				for (int i = 0; i < _list.size(); i++)
				{

					Notification p = _list.get(i);
					if( !p.deleted)
					{
						toReturn.add(p);
					}

				}
					return toReturn;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		_list = new ArrayList<>();
		return _list;
	}

	private static JSONArray saveToJson(ArrayList<Notification> list)
	{
		JSONArray array = new JSONArray();
		for( Notification sp : list)
		{
			if( sp.message != null)
			{
				array.put(sp.serialize());
			}
		}

		return  array;
	}

	private static ArrayList<Notification> loadFromJson(JSONArray array)
	{
		ArrayList<Notification> list = new ArrayList<>();
		try
		{

			for (int i = 0; i < array.length(); i++)
			{
				JSONObject push = array.getJSONObject(i);
				Notification storedPush = new Notification(null,null);
				storedPush.deserialize(push);
				list.add(storedPush);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Collections.sort(list, new Comparator<Notification>()
		{
			@Override
			public int compare(Notification lhs, Notification rhs)
			{
				return (int)( rhs.timestamp.getTime()-lhs.timestamp.getTime());
			}
		});

		return list;
	}

	public static boolean alreadyContains(Notification push, Context context)
	{


		for (int i = 0; i < _list.size(); i++)
		{
			Notification p = _list.get(i);
			String pid= p.getUID();
			String id = push.getUID();
			if( id != null &&  id.equals(pid))
			{
				return	true;
			}
		}

		return false;
	}

	public static String getAllIds(Context context)
	{

		getStored(context);
		StringBuilder builder = new StringBuilder("[");
		synchronized (_list)
		{




			for (int i = 0; i < _list.size(); i++)
			{
				Notification p = _list.get(i);
				String pid= p.getCode();
				builder.append(pid).append(", ");

			}
			builder.append("]");


		}

		return builder.toString();
	}
}

