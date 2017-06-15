package com.tendarts.sdk.communications;

/**
 * Created by jorgearimany on 10/6/17.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jorgearimany on 30/3/15.
 */
public class Bitmaps
{


	private static final int MAX_SIZE = 512;//max image size in pixels, any direction.
	private static final int MAX_SIZE_H = 320;//maxi image size in heitht

	private static final String TAG = "Bitmaps";



	public static boolean lowMemory = false;

	public static Bitmap bitmapFromFile(int width, int height, String file)
	{
		return bitmapFromFile( width,  height,  file, false);
	}
	public static Bitmap bitmapFromFile(int width, int height, String file, boolean fullColor )
	{
		return bitmapFromFile( width,  height,  file, false, false);
	}
	public static Bitmap bitmapFromFile(int width, int height, String file, boolean fullColor, boolean mutable )
	{

		Bitmap bitmap = null;

		final BitmapFactory.Options options = new BitmapFactory.Options();

		Log.i(TAG, "Bitmap from file: "+width+"," + height+ "calculating factor");

		if( height <1)
		{
			height = MAX_SIZE_H;
		}
		if( width < 1)
		{
			width = MAX_SIZE;
		}
		if( height >0 && width > 0 ) {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file,options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, width, height);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

		}
		else
		{
			options.inSampleSize = 2;
		}

		try
		{
			if( !fullColor)
			{
				options.inPreferredConfig = Bitmap.Config.RGB_565;
			}
			if( mutable)
			{
				options.inMutable=true;
			}

			Log.i(TAG, "decoding factor:" + options.inSampleSize+ " " +width+","+height+ " for file:"+file);

			bitmap = BitmapFactory.decodeFile(file, options);

			//rotate bitmap in case
			ExifInterface exif = new ExifInterface(file);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
			Log.i(TAG, "orientation: " + orientation);
			Matrix matrix = new Matrix();
			switch (orientation) {
				case ExifInterface.ORIENTATION_NORMAL:
					return bitmap;
				case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
					matrix.setScale(-1, 1);
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					matrix.setRotate(180);
					break;
				case ExifInterface.ORIENTATION_FLIP_VERTICAL:
					matrix.setRotate(180);
					matrix.postScale(-1, 1);
					break;
				case ExifInterface.ORIENTATION_TRANSPOSE:
					matrix.setRotate(90);
					matrix.postScale(-1, 1);
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					matrix.setRotate(90);
					break;
				case ExifInterface.ORIENTATION_TRANSVERSE:
					matrix.setRotate(-90);
					matrix.postScale(-1, 1);
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					matrix.setRotate(-90);
					break;
				default:
					return bitmap;
			}
			try {
				Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				bitmap.recycle();
				return bmRotated;
			}
			catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			}
		}
		catch (Exception e)
		{

			e.printStackTrace();
			if( bitmap != null)
			{
				bitmap.recycle();
			}
		}
		catch (Throwable t)
		{
			Log.e(TAG, "Throwable catched!!!" + t.getMessage());
			t.printStackTrace();
			if( bitmap != null)
			{
				bitmap.recycle();
			}
			return null;

		}


		return bitmap;
	}


	public static Bitmap bitmapFromStream(int width, int height,  InputStream buffIn)
	{

		try
		{
			buffIn.mark(buffIn.available());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		Bitmap bitmap;

		if( height <1)
		{
			height = MAX_SIZE;
		}
		if( width < 1)
		{
			width = MAX_SIZE;
		}
		if( height >0 && width > 0 ) {
			options.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(buffIn, null, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, width, height);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

		}
		else
		{
			options.inSampleSize = 1;
		}

                /*
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(buffIn,null, options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                String imageType = options.outMimeType;*/

		try
		{
			buffIn.reset();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		bitmap = BitmapFactory.decodeStream(buffIn,null,options);
		return bitmap;
	}


	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {

		long maxsize = Runtime.getRuntime().freeMemory()/6;
		Log.i(TAG, "max free: "+ maxsize);

		if( reqHeight <1)
		{
			reqHeight = MAX_SIZE;
		}

		if( reqWidth < 1)
		{
			reqWidth = MAX_SIZE;
		}
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height;// / 2;
			final int halfWidth = width;// / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((couldShrink(halfWidth, reqWidth, inSampleSize)&&
					couldShrink(halfHeight,reqHeight, inSampleSize))
				//&&(halfHeight*halfWidth)/inSampleSize > maxsize)
					)
			{
				inSampleSize *= 2;
			}

			//check for memory
			if( (height*width)/inSampleSize > 1920*1080 &&

					((halfHeight / inSampleSize) > reqHeight
							|| (halfWidth / inSampleSize) > reqWidth ))
			{
				inSampleSize *= 2;
			}
			if( height / inSampleSize <4 && width/ inSampleSize < 4 && inSampleSize > 1)
			{
				inSampleSize /=2;
			}
		}

		return inSampleSize;//*2;
	}

	private static  boolean couldShrink ( int dimension, int req_dimension, int divider)
	{
		int actual = dimension / divider;
		int next = dimension / (divider*2);

		int next_error = Math.abs(next - req_dimension);
		int actual_error = Math.abs(actual-req_dimension);




		return next > req_dimension ||
				(actual > req_dimension && (next_error < actual_error) )
				;
	}

	public static boolean canUseForInBitmap(
			Bitmap candidate, BitmapFactory.Options targetOptions)
	{

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// From Android 4.4 (KitKat) onward we can re-use if the byte size of
			// the new bitmap is smaller than the reusable bitmap candidate
			// allocation byte count.
			int width = targetOptions.outWidth / targetOptions.inSampleSize;
			int height = targetOptions.outHeight / targetOptions.inSampleSize;
			int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
			return byteCount <= candidate.getAllocationByteCount();
		}

		// On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
		return candidate.getWidth() == targetOptions.outWidth
				&& candidate.getHeight() == targetOptions.outHeight
				&& targetOptions.inSampleSize == 1;
	}

	/**
	 * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
	 */
	static int getBytesPerPixel(Bitmap.Config config) {
		if (config == Bitmap.Config.ARGB_8888) {
			return 4;
		} else if (config == Bitmap.Config.RGB_565) {
			return 2;
		} else if (config == Bitmap.Config.ARGB_4444) {
			return 2;
		} else if (config == Bitmap.Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}
}
