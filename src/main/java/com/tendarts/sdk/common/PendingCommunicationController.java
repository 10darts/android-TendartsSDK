package com.tendarts.sdk.common;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.tendarts.sdk.client.TendartsClient;
import com.tendarts.sdk.communications.Communications;
import com.tendarts.sdk.communications.ICommunicationObserver;
import com.tendarts.sdk.communications.NetworkChangeReceiver;
import com.tendarts.sdk.communications.PendingCommunicationsService;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.tendarts.sdk.communications.Communications.CONNECTION_TIMEOUT;

/**
 * Created by jorgearimany on 5/10/17.
 */

public class PendingCommunicationController {

	private static final int MAX_RETRIES = 10;
	private static final String TAG = "PendingCommunication";
	public static final String TRACE = "trace";
	private static  ArrayList<Communications.PendingCommunication> pendingCommunications= new ArrayList<>();
	private static String pendingTokenStack = null;
	private static int tokenRetries=0;
	private static String pendingLink = null;

	public static void doPending(final Context context) {
		//check internet

		if (!ConnectionManager.isConnected(context)) {
			NetworkChangeReceiver.enable(context);
			LogHelper.logConsole(TAG, "pending: no internet");
			return;
		}
		LogHelper.logConsole(TAG, "doing pending ");
		load(context);

		String code =  Configuration.instance(context).getPushCode();
		if (code == null|| code.length()< 2) {
			LogHelper.logConsole(TAG, "doPending: no code");
			String token = Configuration.instance(context).getPush();
			if (token != null && token.length() > 2) {
				LogHelper.logConsole(TAG, "doPending: no code but token exists");
				PushController.sendTokenAndVersion(token,context);
				return;
			} else {
				LogHelper.logConsole(TAG, "doPending: no code nor token");
				return;
			}
		}
		doPendingLink(context);

		List<Communications.PendingCommunication> list;
		synchronized (pendingCommunications) {
			 list = (List<Communications.PendingCommunication>) pendingCommunications.clone();
		}
		HttpUriRequest request = null;
		for (final Communications.PendingCommunication pending :
				list) {
			StringEntity se = null;
			if (pending.body != null) {

				String body = pending.body;
				try {
					JSONObject object = new JSONObject(body);
					object.put(TRACE, pending.errorStack);
					body = object.toString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					se = new StringEntity(body, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}


			}
			switch ( pending.method ) {
				case GET:
					HttpGet get = new HttpGet( getUrl( pending));
					request = get;
				break;
				case PUT:
					HttpPut put = new HttpPut(getUrl( pending));
					if( se != null)
					{
						put.setEntity(se);
					}
					request = put;
					break;
				case POST:
					HttpPost post = new HttpPost(getUrl( pending));
					if( se != null)
					{
						post.setEntity(se);
					}
					request = post;
					break;
				case PATCH:
					HttpPatch patch = new HttpPatch(getUrl( pending));
					if( se != null)
					{
						patch.setEntity(se);
					}
					request = patch;
					break;
			}

			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
			HttpResponse response;
			try {

				for (Header h:pending.headers
					 ) {
					request.addHeader(h.getName(),h.getValue());
				}

				Communications.CommunicationsThread thread = new Communications.CommunicationsThread(new ICommunicationObserver() {
					@Override
					public void onSuccess(int operationId, JSONObject data) {
						sent(pending, context);
					}

					@Override
					public void onFail(int operationId, String reason, Communications.PendingCommunication pc) {
						failed(pending, reason, context);
						if (!ConnectionManager.isConnected(context)) {
							NetworkChangeReceiver.enable(context);
							return;
						}
					}
				}, 0, client, request, false, pending);

                LogHelper.logConsole(TAG, "sending "+ pending.url	);

                thread.run();

			} catch (Exception ex) {
				failed(pending,ex.getMessage(), context);
				if (!ConnectionManager.isConnected(context)) {
					NetworkChangeReceiver.enable(context);
					return;
				}
			}

		}
		//SI QUEDAN PONER TIMER
		scheludePendingIfAny(context);
	}

	private static void load(Context context) {

		synchronized (PendingCommunicationController.class) {
			pendingLink = Configuration.instance(context).getPendingLink();
			tokenRetries = Configuration.instance(context).getTokenRetries();
			pendingTokenStack= Configuration.instance(context).getPendingToken();
		}
		String data = Configuration.instance(context).getPendingCommunications();
		if (data != null && data.length() > 0) {
			try {
				synchronized (pendingCommunications) {
					pendingCommunications.clear();
					JSONArray array = new JSONArray(data);
					for (int i = 0; i < array.length(); i++) {
						pendingCommunications.add( new Communications.PendingCommunication(array.getJSONObject(i)));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void save(Context context) {

		try {
			synchronized (PendingCommunicationController.class) {
				Configuration.instance(context).setPendingLink(pendingLink);
				Configuration.instance(context).setPendingToken(pendingTokenStack);
				Configuration.instance(context).setTokenRetries(tokenRetries);
			}
			synchronized (pendingCommunications) {
				JSONArray array = new JSONArray();
				for (Communications.PendingCommunication pending :
						pendingCommunications) {
					array.put(pending.serialize());
				}
				Configuration.instance(context).setPendingCommunications(array.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void scheludePendingIfAny(Context context) {

		if ((pendingCommunications == null || pendingCommunications.size() < 1)
				&& pendingTokenStack == null && pendingLink == null) {
			return;
		}
		if (!ConnectionManager.isConnected(context)) {
			NetworkChangeReceiver.enable(context);
			return;
		}
		PendingCommunicationsService.schedulePending(1800000,context);
	}

	private static void failed(Communications.PendingCommunication pending, String reason, Context context) {

		LogHelper.logConsole(TAG, "failed: "+pending.url +" :"+reason);
		synchronized (pendingCommunications) {
			Communications.PendingCommunication current;
			for (int i = 0; i < pendingCommunications.size(); i++) {

				current = pendingCommunications.get(i);
				if (current.timestamp == pending.timestamp) {
					current.errorStack += reason;
					current.nRetries++;
					if (current.nRetries >MAX_RETRIES) {
						pendingCommunications.remove(i);
						commitLastError(current);
					}
						break;
				}

			}
		}
		save(context);
	}

	private static void commitLastError(Communications.PendingCommunication current) {

		try {
			LogHelper.logConsole(TAG, "commitLastError: "+current.errorStack);
			String uri = Uri.parse("http://10darts.com/api/v1/log/")
					.buildUpon()
					.appendQueryParameter("log", current.errorStack)
					.appendQueryParameter("retriew", ""+current.nRetries)
					.build().toString();
			HttpGet get = new HttpGet(uri);
			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
			HttpResponse response;
			Communications.CommunicationsThread thread = new Communications.CommunicationsThread(null,0,client,get,false,null);
			thread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void sent(Communications.PendingCommunication pending, Context context) {

		LogHelper.logConsole(TAG, "sent "+pending.url);
		synchronized (pendingCommunications) {
			int index = -1;
			for (int i = 0; i < pendingCommunications.size(); i++) {
				if (pendingCommunications.get(i).timestamp == pending.timestamp) {
					index = i;
					break;
				}
			}
			if (index >=0) {
				pendingCommunications.remove(index);
			}
		}
		save(context);
	}

	private static String getUrl(Communications.PendingCommunication pending) {
		return pending.url+"?retries="+pending.nRetries;
	}

	public static void addPending(Communications.PendingCommunication pending, Context context) {

		LogHelper.logConsole(TAG, "addPending: ");
		load(context);
		synchronized (pendingCommunications) {
			pendingCommunications.add(pending);
		}
		save(context);
		if (!ConnectionManager.isConnected(context)) {
            LogHelper.logConsole(TAG, "addPending: enabling network change receiver");
			NetworkChangeReceiver.enable(context);
		}
	}

	public static void addPendingToken(String log,Context context) {

		LogHelper.logConsole(TAG, "addPendingToken: ");
		load(context);
		tokenRetries ++;
		pendingTokenStack += "\n"+log;
		save(context);
	}

	public static void addPendingTokenInfo(JSONObject object, Context context) {
		
		load(context);
		if (pendingTokenStack != null && object != null) {
			String pending = pendingTokenStack;
			if(object.has(TRACE)&& !object.isNull(TRACE)) {
				try {
					pending = object.getString(TRACE)+pending;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				object.put(TRACE,pending);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	public static void onTokenSent(Context context) {

		LogHelper.logConsole(TAG, "onTokenSent:");
		pendingTokenStack = null;
		tokenRetries = 0;
		save(context);
		PendingCommunicationsService.startPendingCommunications(context);

	}

	public static void setPendingLink(String userIdentifier, Context context) {

		LogHelper.logConsole(TAG, "setPendingLink: ");
		pendingLink = userIdentifier;
		save(context);

	}
	private static void doPendingLink(final Context context) {

		LogHelper.logConsole(TAG, "doPendingLink: ");
		try {
			if (pendingLink == null) {
				LogHelper.logConsole(TAG, "doPendingLink: no pending link");
				return;
			}
			if (Configuration.getAccessToken(context)== null) {
				LogHelper.logConsole(TAG, "doPendingLink: not init");
				return;
			}

			String code = Configuration.instance(context).getPushCode();
			if (code == null || code.length() < 3) {
				LogHelper.logConsole(TAG, "doPendingLink: no code");
				return;
			}

			final JSONObject object = new JSONObject();
			object.put("client_data",pendingLink);
			String deviceId = String.format( Constants.DEVICE_REFERENCE,code);

			object.put("device",deviceId );

			Communications.postData(Constants.LINKS, Util.getProvider(), 0, new ICommunicationObserver() {
				@Override
				public void onSuccess(int operationId, JSONObject data) {
					if (data.has("persona") && !data.isNull("persona")) {
						try {
							String resourceUri = data.getString("persona");
							Configuration.instance(context).setUserCode(resourceUri);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					pendingLink = null;
					save(context);
					TendartsClient.instance(context).onUserLinkedToDevice();
				}

				@Override
				public void onFail(int operationId, String reason,
								   Communications.PendingCommunication pc) {
					PendingCommunicationController.addPending(pc,context);
					pendingLink = null;//already in pending list
					save(context);
				}
			},object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
