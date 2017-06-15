package com.tendarts.sdk.communications;

import org.json.JSONArray;

/**
 * Created by jorgearimany on 10/6/17.
 */

public interface ICommunicationArrayObserver
{
	public void onSuccess( int operationId, JSONArray data);
	public void onFail(int operationId, String reason);

}
