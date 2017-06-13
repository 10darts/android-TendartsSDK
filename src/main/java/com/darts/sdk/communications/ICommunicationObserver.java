package com.darts.sdk.communications;

import org.json.JSONObject;

/**
 * Created by jorgearimany on 10/6/17.
 */

public interface ICommunicationObserver
{
	public void onSuccess( int operationId, JSONObject data);
	public void onFail(int operationId, String reason);
}
