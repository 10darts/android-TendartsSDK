package com.tendarts.sdk.communications;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by jorgearimany on 22/1/15.
 */
public class ImageDownloadThread extends Thread {

	private static final String TAG = "OP:ImgThread:";


	/**
	 * Observer to be called, it could be null
	 */
	private IImageDownloadObserver _observer;

	/**
	 * Operation id
	 */
	private int _id;


	private URL _url;

	private String _file;

	private int _width;
	private int _height;

	public ImageDownloadThread( IImageDownloadObserver observer, int id,
								URL url, String file, int width, int height)
	{
		assert(url != null);

		_observer = observer;
		_id = id;
		_url = url;
		_file = file;
		_width = width;
		_height = height;
	}


	/**
	 * Performs the operation.
	 */
	@Override
	public void run()
	{
		try
		{


			URLConnection conn = _url.openConnection();
			conn.connect();

			InputStream is = conn.getInputStream();

			Bitmap img = null;
			if( _file != null)
			{

				try
				{

					Log.i(TAG,"xxx downloading to file: "+_file);

					FileOutputStream fos = new FileOutputStream(_file);

					byte data[] = new byte[4086];

					int count = 0;
					while ((count = is.read(data)) != -1)
					{
						fos.write(data, 0, count);
					}

					is.close();
					fos.flush();
					fos.close();


					int len =  conn.getContentLength();
					File f = new File( _file);

					if( len== f.length())
					{


						Log.i(TAG, "xxx downloaded: " + _file );
						//got image, read it and notify observers.


						img = Bitmaps.bitmapFromFile(_width, _height, _file);
						Log.i(TAG, "xxx decoded: " + (img != null));
					}
					else
					{
						Log.e(TAG, "xxx error downloaidng "+_url+ "\n" + len + "\n" + f.length());
						File tmp = new File( _file);
						if( tmp.exists())
						{
							tmp.delete();
						}

					}
				}
				catch (Exception e )
				{
					try
					{
						e.printStackTrace();
						//delete file with errors
						File tmp = new File( _file);
						if( tmp.exists())
						{
							tmp.delete();
						}

					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				finally
				{
					is.close();

				}

			}
			else
			{


				BufferedInputStream bis = new BufferedInputStream(is, 8190);

				ByteArrayBuffer baf = new ByteArrayBuffer(50);
				int current = 0;
				while ((current = bis.read()) != -1)
				{
					baf.append((byte) current);
				}
				byte[] imageData = baf.toByteArray();


				img = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);//BitmapFactory.decodeStream(conn.getInputStream());
			}
			//check response
			if(img!=null)
			{
				if( _observer != null)
				{

					_observer.onSuccess(_id,img);

				}
			}
			else
			{
				if(_observer != null)
				{
					_observer.onFail(_id, "No response");
				}
			}
		}
		catch (Exception ex)
		{
			if( _observer != null)
			{
				_observer.onFail(_id, ex.getMessage());
			}
			Log.e(TAG, "Error running thread:" + ex.getMessage());
			ex.printStackTrace();
		}

	}
}