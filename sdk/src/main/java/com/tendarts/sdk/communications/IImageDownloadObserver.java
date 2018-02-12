package com.tendarts.sdk.communications;

import android.graphics.Bitmap;

/**
 * Created by jorgearimany on 10/6/17.
 */

public interface IImageDownloadObserver {
	public void onSuccess( int operationId, Bitmap data);
	public void onFail(int operationId, String reason);

}

