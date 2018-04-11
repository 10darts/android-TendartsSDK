package com.tendarts.sdk.communications;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.tendarts.sdk.common.LogHelper;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtilsHC4;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jorgearimany on 10/6/17.
 */

public class Communications
{

	/**
	 * Created by Jorge Arimany on 8/1/15.
	 */


	//----------------------------------------------------------------------------------------------
	//          INTERFACES
	//----------------------------------------------------------------------------------------------

	public static class CHeader
	{
		String name;
		String value;

		public CHeader(String name, String value)
		{
			this.name = name;
			this.value = value;
		}
	}

	public interface ICommunicationsConfigProvider
	{
		String getPushCode(); //Configuration.instance().getPushCode();

		String getGeostatsUrlFormat();//Constants.geoStats

		void onGeostatSent(boolean success, String info);

		ArrayList<CHeader> getHeaders();
		/*
		request.addHeader("Authorization", OAuth.getHeader());
				request.addHeader("Accept-Language", Configuration.getCurrentLanguage());
				String code = Configuration.instance().getPushCode();
				if (code != null)
				{
					request.addHeader("DeviceId", code);
				}
		 */
	}


	//----------------------------------------------------------------------------------------------
	//          CONSTANTS
	//----------------------------------------------------------------------------------------------

	public static final int CONNECTION_TIMEOUT = 10000;
	private static final String TAG = "OP:Communications:";
	public static final int MIN_ACCURACY = 1500;


	//----------------------------------------------------------------------------------------------
	//          PRIVATE MEMBERS
	//----------------------------------------------------------------------------------------------
	private static double _latitude;
	private static double _longitude;
	private static String _geoHeader;
	private static Context _context;
	private static LocationManager _locationManager;
	private static Date _lastLocationTime;


	//private static ICommunicationsConfigProvider _provider=null;

	private static HttpClientContext _localContext;
	private static CookieStore _cookieStore;

	private static Object sync = new Object();

	private static WeakReference<ILocationAlerter> _alerterReference = null;

	//-------------------------------------------
	//		LOCATION STATS
	//-------------------------------------------
	static boolean locationServiceAvailable = true;
	public static String lastProvider = "";
	public static String lastSource = "";
	public static boolean outOfRange = false;
	public static float lastPrecission = Float.MAX_VALUE;
	public static float lastSentPrecission = Float.MAX_VALUE;


	//----------------------------------------------------------------------------------------------
	//          PUBLIC ACCESS
	//----------------------------------------------------------------------------------------------


	public static void init(Context c)
	{

		_context = c;
		// Create a local instance of cookie store
		_cookieStore = new BasicCookieStore();

		// Create local HTTP context
		_localContext = HttpClientContext.create();
		// Bind custom cookie store to the local context
		_localContext.setCookieStore(_cookieStore);


		_locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
		Log.i(TAG, "location manager created: " + _locationManager + " context " + _context);

	}

	public static Context getContext()
	{
		return _context;
	}

	// check network connection
	public static boolean isConnected()
	{

		assert (_context != null);//not initialized!!

		if (_context != null)
		{
			ConnectivityManager connMgr;
			connMgr = (ConnectivityManager) _context.getSystemService(_context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected())
				return true;
			else
				return false;
		}

		return false;
	}




  /*  private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();
        return result;

    }*/

	public static String convertStreamToString(InputStream stream) throws Exception
	{

 /*
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";

        byte[] bytes = new byte[10000];

        StringBuilder x = new StringBuilder();

        int numRead = 0;
        while ((numRead = stream.read(bytes)) >= 0) {
            x.append(new String(bytes, 0, numRead));
        }
        return x.toString();
        /**/

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null)
		{
			stringBuilder.append(line); //+ "\n");
		}
		stream.close();
		return stringBuilder.toString();
	}


	/**
	 * Sets the geolocation for api calls
	 *
	 * @param latitude
	 * @param longitude
	 */
	public static void setGeolocation(double latitude, double longitude)
	{
		synchronized (sync)
		{
			_latitude = latitude;
			_longitude = longitude;
			_geoHeader = "<geo:" + latitude + ";" + longitude + ">";
		}
		//sendGeoStats();
	}

	public static double getLatitude()
	{
		return _latitude;
	}

	public static double getLongitude()
	{
		return _longitude;
	}

	public static double getLastPrecission()
	{
		return lastPrecission;
	}

	public static String getLastProvider()
	{
		return lastProvider;
	}

	public static String getLastSource()
	{
		return lastSource;
	}

	private static Date lastSentStats = new Date(0);
	private static int nSentStats = 0;

	public static void sendGeoStats(final ICommunicationsConfigProvider provider)
	{
		final Date now = new Date();
		long elapsed = now.getTime() - lastSentStats.getTime();

		if (nSentStats < 3 || elapsed > 120000)
		{


			try
			{
				if (provider == null)
				{
					return;
				}

				String code = provider.getPushCode();
				if (code == null || code.length() < 2)
				{
					return;
				}

				boolean enabled = isLocationEnabled();
				boolean on = isLocationON();
				String origin = "";
				String debug = "";
				if (!enabled)
				{
					origin += "NE ";
				}
				if (!on)
				{
					origin += "OFF ";
				}
				if (_latitude != 0 && _longitude != 0)
				{

					origin += lastProvider;
					debug += lastSource;
				} else
				{
					origin += outOfRange ? "OOR:" + lastProvider : " NoData";

					debug += "LS:" + locationServiceAvailable + " " + lastSource;
				}
				debug += "|acc:" + lastPrecission;

				JSONObject obj = new JSONObject();
				obj.put("last_position_origin", origin);
				obj.put("debug_info", debug);

				addGeoData(obj);
				String url = String.format(provider.getGeostatsUrlFormat(), code);
				LogHelper.logConsole(TAG, "sendGeoStats: patch to " + url + "\n" + obj + enabled);
				patchData(url, provider, 0, new ICommunicationObserver()
				{
					@Override
					public void onSuccess(int operationId, JSONObject data)
					{
						nSentStats++;
						lastSentStats = now;
						provider.onGeostatSent(true, data.toString());


					}

					@Override
					public void onFail(int operationId, String reason, PendingCommunication pending)
					{
						provider.onGeostatSent(false, reason);
						Log.e(TAG, "sendGeoStats error:" + reason);
					}
				}, obj.toString(), false);

			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}


	//todo: extract to ohter place
	public static class LatLong
	{
		public double latitude;
		public double longitude;

		public LatLong()
		{
		}

		;

		public LatLong(double lat, double longit)
		{
			latitude = lat;
			longitude = longit;
		}
	}

	public static LatLong getCurrentGeolocation()
	{
		updateGeolocation();
		return new LatLong(_latitude, _longitude);
	}


	public static boolean isLocationON()
	{
		try
		{
			int locationMode = 0;
			String locationProviders;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				try
				{
					locationMode = Settings.Secure.getInt(_context.getContentResolver(), Settings.Secure.LOCATION_MODE);

				} catch (Settings.SettingNotFoundException e)
				{
					e.printStackTrace();
				}

				return locationMode != Settings.Secure.LOCATION_MODE_OFF;

			} else
			{
				locationProviders = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				return !TextUtils.isEmpty(locationProviders);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isLocationEnabled()
	{
		try
		{
			if (_locationManager == null && _context != null)
			{
				_locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
				if (_locationManager == null)
				{
					locationServiceAvailable = false;
				}

			}
			if (_locationManager != null)
			{

				String provider = getGProvider();
				if(provider == null)
				{
					return false;
				}
				Boolean enabled = _locationManager
						.isProviderEnabled(provider);
				return enabled;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;

	}

	private static String getGProvider()
	{
		try
		{
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setSpeedRequired(false);
			criteria.setCostAllowed(true);
			String provider = _locationManager.getBestProvider(criteria, true);
			lastProvider = provider;
			return provider;
		} catch (Exception e)
		{
			e.printStackTrace();
			lastProvider = "EXC";

		}
		return LocationManager.NETWORK_PROVIDER;
	}


	public static void setLocationAlerter(ILocationAlerter alerter)
	{
		_alerterReference = new WeakReference<ILocationAlerter>(alerter);

	}

	public static void alertNoLocation(Activity parent)
	{
		try
		{
			if (_alerterReference == null)
			{
				return;
			}

			ILocationAlerter _alerter = _alerterReference.get();
			if (_alerter != null && !isLocationEnabled())
			{
				_alerter.alertNotEnabled(parent);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void updateGeolocation()
	{
		try
		{
			Log.i(TAG, "updating geolocation");
			if (isLocationEnabled())
			{
				Date now = new Date();

				boolean askAgain = _lastLocationTime == null || ((now.getTime() - _lastLocationTime.getTime()) > 60000);
				Location location = null;
				Log.i(TAG, "location enabled");
				if (_locationManager != null)
				{

					if ((_latitude == 0 && _longitude == 0) || askAgain)
					{

						try
						{
							location = _locationManager.getLastKnownLocation(getGProvider());
							if (location != null)
							{
								Log.i(TAG, "location new location " + location.getLatitude() +
										" p:" + location.getProvider() + " acc:" + location.getAccuracy());
							}
						} catch (SecurityException se)
						{
							se.printStackTrace();
						} catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				} else
				{
					Log.i(TAG, "location manager not found");
				}
				if (location != null)
				{
					float acc = location.getAccuracy();
					lastSource = "LastKnown";
					lastProvider = location.getProvider();
					lastPrecission = location.getAccuracy();
					if (acc < MIN_ACCURACY && acc != 0)
					{
						outOfRange = false;
						setGeolocation(location.getLatitude(), location.getLongitude());

						_lastLocationTime = new Date();
						_lastLocationTime = now;
					} else
					{
						outOfRange = true;

					}
				} else
				{

					try
					{
						if (_alerterReference == null)
						{
							return;
						}

						ILocationAlerter _alerter = _alerterReference.get();
						//alert location
						if (_alerter != null && askAgain)
						{
							try
							{
								_alerter.alertNotEnabled(null);
							} catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}


			} else
			{
				Log.i(TAG, "location not enabled");
			}


		} catch (Exception e)
		{
			e.printStackTrace();
		}


	}

	static long updateCount = 0;

	private static void setHeaders(org.apache.http.client.methods.HttpUriRequest request,
								   ICommunicationsConfigProvider _provider)
	{
		try
		{
			if (updateCount++ % 100 == 0)
			{
				updateGeolocation();
			}
			synchronized (sync)
			{
				request.addHeader("Geolocation", _geoHeader);
				request.addHeader("Content-Type", "application/json");

				try
				{
					if (_provider != null)
					{
						ArrayList<CHeader> headers = _provider.getHeaders();
						if (headers != null)
						{
							for (CHeader header : headers)
							{
								request.addHeader(header.name, header.value);
							}
						}
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				LogHelper.logConsole(TAG, "Geolocation:" + _geoHeader);
			}


		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}


	public static void putData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
							   String payload, boolean plain)
	{
		putData(url, provider, id, observer, payload, plain, false);
	}

	public static void putData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
							   String payload, boolean plain, boolean synchronous)
	{
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		PendingCommunication pending = null;
		try
		{


			HttpPut post = new HttpPut(url);

			if (plain)
			{
				post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			} else
			{
				setHeaders(post, provider);
			}
			pending = new PendingCommunication(PendingCommunication.Method.POST,
					payload,post.getAllHeaders(),url);
			if (payload != null)
			{
				String body = payload;
				StringEntity se = new StringEntity(body, "UTF-8");
				post.setEntity(se);
			}
			CommunicationsThread thread = new CommunicationsThread(observer, id, client, post, plain, pending);
			if (!synchronous)
			{
				thread.start();
			} else
			{
				thread.run();
			}


		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause(),pending);
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
		}

	}

	public static void patchData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
								 String payload, boolean plain)
	{
		patchData(url, provider, id, observer, payload, plain, false);
	}

	public static void patchData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
								 String payload, boolean plain, boolean synchronous)
	{
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		PendingCommunication pending= null;
		try
		{



			HttpPatch post = new HttpPatch(url);

			if (plain)
			{
				post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			} else
			{
				setHeaders(post, provider);
			}
			pending = new PendingCommunication(PendingCommunication.Method.PATCH,
				payload,post.getAllHeaders(),url);

			if (payload != null)
			{
				String body = payload;
				StringEntity se = new StringEntity(body, "UTF-8");
				post.setEntity(se);
			}
			CommunicationsThread thread = new CommunicationsThread(observer, id, client, post, plain, pending);
			if (!synchronous)
			{
				thread.start();
			} else
			{
				thread.run();
			}

		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause(),pending);
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
		}

	}


	public static void postData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
								String payload)
	{
		postData(url, provider, id, observer, payload, false, false);
	}

	public static void postData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
								String payload, boolean plain)
	{
		postData(url, provider, id, observer, payload, plain, false);
	}

	public static void postData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer,
								String payload, boolean plain, boolean synchronous)
	{
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		PendingCommunication pending = null;
		try
		{


			HttpPost post = new HttpPost(url);

			if (plain)
			{
				post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			} else
			{
				setHeaders(post, provider);
			}
			pending = new PendingCommunication(PendingCommunication.Method.POST,
					payload,post.getAllHeaders(),url);
			if (payload != null)
			{
				String body = payload;
				StringEntity se = new StringEntity(body, "UTF-8");
				post.setEntity(se);
			}

			CommunicationsThread thread = new CommunicationsThread(observer, id, client, post, plain, pending);
			if (!synchronous)
			{
				thread.start();
			} else
			{
				thread.run();
			}

		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause(),pending);
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
		}

	}


	public static void deleteData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer
	)
	{
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		try
		{

			HttpDelete delete = new HttpDelete(url);


			setHeaders(delete, provider);


			CommunicationsThread thread = new CommunicationsThread(observer, id, client, delete, false, null);
			thread.start();

		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause(),null);
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
		}

	}


/*
    public static void postData( String url,
                                 int id,
                                 ICommunicationObserver observer,
                                 JSONObject json)
    {


        /*

        HttpClient client = new DefaultHttpClient();

        HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
        HttpResponse response;
        try
        {

            HttpPost post = new HttpPost(url);

            StringEntity se = new StringEntity(json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            setHeaders(post);
            response = client.execute(post);
			//Checking response
            if(response!=null){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                String result=null;
                result = convertStreamToString(in);
                JSONObject objectResult = new JSONObject(result);
                //String guid = objectResult.getString(Constants.REGISTER_RETURN_OBJECT);
                //Log.d(Constants.DEBUG_TAG_COMMUNICATION_MODULE, "RegisterDevice GUID: " + guid);
                //JSONArray apps = objectResult.getJSONArray(Constants.REGISTER_OUTPUT_APPLICATIONS);
               // Log.d(Constants.DEBUG_TAG_COMMUNICATION_MODULE, "JSONArray");
               // if (guid.length()>10) {
                //    Log.d(Constants.DEBUG_TAG_COMMUNICATION_MODULE, "Device has been registered successfully.");
                 //   _guid = guid;
                 //   _deviceName = terminalName;
                 //   _license = license;
                 //return Constants.RegisterResult.REGISTERED;
                //}
            }
            //Log.e(Constants.DEBUG_TAG_COMMUNICATION_MODULE, "Error registering device.");
            //return Constants.RegisterResult.REGISTER_ERROR;
        }
        catch(Exception ex){
            //Log.e(Constants.DEBUG_TAG_COMMUNICATION_MODULE, "Error registering device. Error: " + ex.getMessage() +
             //       ". StackTrace: " + ex.getStackTrace());
            //return Constants.RegisterResult.REGISTER_ERROR;
        }


    }
*/


	public interface IChunkObserver
	{
		void onProgress(float progress);
	}


	public static class ChunkResult
	{
		public int statusCode;
		public JSONObject json;
		public String error;
		public String uploadId;
	}

	static long dataSent = 0;
	static long totalSent = 0;

	public static ChunkResult postChunk(String url, ICommunicationsConfigProvider provider, int start, int end, int total, final RandomAccessFile file, String uploadId, String caption, String position, final IChunkObserver observer, String filename)
	{
		ChunkResult result = new ChunkResult();
		result.statusCode = -1;
		result.json = new JSONObject();
		try
		{
			final int count = 1 + end - start;
			;
			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
			//HttpResponse response;

			byte[] buffer = new byte[count];


			file.seek(start);

			int c2 = file.read(buffer, 0, count);
			if (c2 < 1 || c2 != count)
			{
				Log.w(TAG, "postChunk: different chunks:" + count + "," + c2);
			}


			HttpPost request = new HttpPost(url);
			setHeaders(request, provider);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//.BROWSER_COMPATIBLE);
			if (uploadId != null && start != 0)
			{
				builder.addPart("upload_id", new StringBody(uploadId));
			}
			if (caption != null)
			{
				builder.addPart("caption", new StringBody(caption));
			}
			if (position != null)
			{
				builder.addPart("position", new StringBody(position));
			}


			builder.addBinaryBody("file", buffer, ContentType.DEFAULT_BINARY, filename == null ? "chunk" : filename);


			//builder.setBoundary("fkldjsalkfjdslka");
			final HttpEntity yourEntity = builder.build();


			class ProgressiveEntity implements HttpEntity
			{
				int iteration;

				@Override
				public void consumeContent() throws IOException
				{
					yourEntity.consumeContent();
				}

				@Override
				public InputStream getContent() throws IOException,
						IllegalStateException
				{
					return yourEntity.getContent();
				}

				@Override
				public Header getContentEncoding()
				{
					return yourEntity.getContentEncoding();
				}

				@Override
				public long getContentLength()
				{
					return yourEntity.getContentLength();
				}

				@Override
				public Header getContentType()
				{
					return yourEntity.getContentType();
				}

				@Override
				public boolean isChunked()
				{
					return yourEntity.isChunked();
				}

				@Override
				public boolean isRepeatable()
				{
					return yourEntity.isRepeatable();
				}

				@Override
				public boolean isStreaming()
				{
					return yourEntity.isStreaming();
				} // CONSIDER put a _real_ delegator into here!

				@Override
				public void writeTo(final OutputStream outStream) throws IOException
				{


					class ProxyOutputStream extends FilterOutputStream
					{

						public ProxyOutputStream(OutputStream proxy)
						{
							super(proxy);
						}

						@Override
						public void write(int idx) throws IOException {
							LogHelper.logConsole(TAG, "upload write idx ");
							out.write(idx);
						}

						@Override
						public void write(byte[] bts) throws IOException {
							LogHelper.logConsole(TAG, "upload write bts " + bts.length);
							int count = bts.length >> 2;
							count++;//testing
							int start = 0;
							while (start < bts.length)
							{
								int remain = count;
								if (start + remain > bts.length)
								{
									remain = bts.length - start;
								}
								write(bts, start, remain);
								start += remain;
							}
							//out.write(bts);
						}

						@Override
						public void write(byte[] bts, int offset, int length) throws IOException {
							LogHelper.logConsole(TAG, "upload write bts st end ");
							out.write(bts, offset, length);
						}

						@Override
						public void flush() throws IOException
						{
							out.flush();
						}

						@Override
						public void close() throws IOException
						{
							out.close();
						}
					} // CONSIDER import this class (and risk more Jar File Hell)

					class ProgressiveOutputStream extends ProxyOutputStream
					{

						long _count = 0;

						Date lastDate = new Date();

						public ProgressiveOutputStream(OutputStream proxy)
						{
							super(proxy);
							lastDate = new Date();

						}

						@Override
						public void write(byte[] buffer, int offset, int byteCount) throws IOException
						{


							_count += byteCount;
							long elapsed = new Date().getTime() - lastDate.getTime();
							//Log.d(TAG, "upload chunk: write ellapsed "+elapsed +" count "+count + " current "+_count);
							if (observer != null && elapsed > 750)
							{

								try
								{

									lastDate = new Date();
									//calculate percentage:
									long length = count;
									if (length > 0) {
										float percentage = 100.0f * _count / length;
										LogHelper.logConsole(TAG, "upload chunk: calling observer percentage " + percentage);
										observer.onProgress(percentage);
									}
								} catch (Exception e)
								{
									e.printStackTrace();
								}
							}
							//Log.i("uploading: " , "o "+offset +"c "+count);
							out.write(buffer, offset, byteCount);
						}


					}
					yourEntity.writeTo(new ProgressiveOutputStream(outStream));
				}

			}
			;
			ProgressiveEntity myEntity = new ProgressiveEntity();

			request.removeHeaders("Content-Type");

			request.setEntity(myEntity);
			request.addHeader(yourEntity.getContentType());

			request.removeHeaders("Content-range");
			String range = String.format("bytes %d-%d/%d", start, end, total);
			request.addHeader("Content-range", range);
			LogHelper.logConsole(TAG, " upload postChunk: " + range);

			if (uploadId != null) {
				request.addHeader("upload_id", uploadId);
			}

			long RequestLength = request.getEntity().getContentLength();

			//execute request
			HttpResponse response = client.execute(request);
			observer.onProgress(100f);
			//check response
			if (response != null)
			{


				int code = response.getStatusLine().getStatusCode();
				result.statusCode = code;
				if (code >= 200 && code < 300)
				{

					dataSent += count;
					totalSent += response.getEntity().getContentLength() + RequestLength;

					if (dataSent > 0) {
						LogHelper.logConsole(TAG, "upload statistics: S" + dataSent + " T" + totalSent + "overhead %" + ((100f * totalSent) / dataSent));
					}

					String res = null;
					if (response.getEntity() != null)
					{
						try
						{
							res = EntityUtilsHC4.toString(response.getEntity(), "utf-8");
							JSONObject object = new JSONObject(res);
							if (object.has("upload_id"))
							{
								result.uploadId = object.getString("upload_id");
							}
							//if( res.contains(""))

						} catch (Exception e)
						{
							e.printStackTrace();

						}
					}


					if (res != null && res.length() > 3)
					{

						result.json = new JSONObject(res);
					}


					Header headers[] = response.getHeaders("Location");

					//ojo no se si hay que hacerlo revisar:
					if (headers != null && headers.length > 0)
					{
						try
						{

							String uri = headers[0].getValue();
							int st = uri.indexOf("/api");
							if (st > 0)
							{
								uri = uri.substring(st);
							}
							result.json.put("resource_uri", uri);
							int cend = uri.length() - 1;
							int cstart = uri.substring(0, end).lastIndexOf("/") + 1;

							result.json.put("code", uri.substring(cstart, cend));
							int id = Integer.parseInt(uri.substring(cstart, cend));
							result.json.put("id", id);

						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					return result;
				} else
				{
					String res = "";
					InputStream in = response.getEntity().getContent();
					res = convertStreamToString(in);
					result.error = res;
				}

			} else //no respose
			{
				result.statusCode = -2;
				return result;
			}
		} catch (Exception ex)
		{
			result.statusCode = -3;
			result.error = ex.getLocalizedMessage();
			Log.e(TAG, "Error running thread:" + ex.getMessage());
			ex.printStackTrace();
			return result;
		}
		return result;
	}


	public static ChunkResult finishFile(String url, ICommunicationsConfigProvider provider, String md5, String uploadId, int order)
	{
		ChunkResult result = new ChunkResult();
		if (uploadId == null)
		{
			result.statusCode = -7;
			result.error = "no upload id";
			return result;
		}
		result.statusCode = -1;
		result.json = new JSONObject();
		try
		{

			HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
			//HttpResponse response;

			HttpPost request = new HttpPost(url);


			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//.BROWSER_COMPATIBLE);

			builder.addPart("upload_id", new StringBody(uploadId));
			builder.addPart("md5", new StringBody(md5));
			builder.addPart("order", new StringBody("" + order));
			//builder.setBoundary("fkldjsalkfjdslka");
			final HttpEntity yourEntity = builder.build();


			request.setEntity(yourEntity);

			setHeaders(request, provider);

			request.removeHeaders("Content-Type");
			request.addHeader(yourEntity.getContentType());


			if (uploadId != null)
			{
				request.addHeader("upload_id", uploadId);
			}


			//execute request
			HttpResponse response = client.execute(request);
			//check response
			if (response != null)
			{


				int code = response.getStatusLine().getStatusCode();
				result.statusCode = code;
				if (code >= 200 && code < 300)
				{


					String res = null;
					if (response.getEntity() != null)
					{
						try
						{
							res = EntityUtilsHC4.toString(response.getEntity(), "utf-8");
							JSONObject object = new JSONObject(res);
							result.json = object;
							LogHelper.logConsole(TAG, "finishFile: " + object);
							//if( res.contains(""))

						} catch (Exception e)
						{
							e.printStackTrace();

						}
					}


					if (res != null && res.length() > 3)
					{

						//	result.json = new JSONObject(res);
					}


					Header headers[] = response.getHeaders("Location");

					//ojo no se si hay que hacerlo revisar:
					if (headers != null && headers.length > 0)
					{
						try
						{

							String uri = headers[0].getValue();
							int st = uri.indexOf("/api");
							if (st > 0)
							{
								uri = uri.substring(st);
							}
							result.json.put("resource_uri", uri);
							int end = uri.length() - 1;
							int start = uri.substring(0, end).lastIndexOf("/") + 1;

							result.json.put("code", uri.substring(start, end));
							int id = Integer.parseInt(uri.substring(start, end));
							result.json.put("id", id);

						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					return result;
				} else
				{
					String res = "";
					InputStream in = response.getEntity().getContent();
					res = convertStreamToString(in);
					result.error = res;
				}

			} else //no respose
			{
				result.statusCode = -2;
				return result;
			}
		} catch (Exception ex)
		{
			result.statusCode = -3;
			result.error = ex.getLocalizedMessage();
			Log.e(TAG, "Error running thread:" + ex.getMessage());
			ex.printStackTrace();
			return result;
		}
		return result;
	}


	public static class Uploader
	{
		public HttpPost post;
		public FileUploadThread thread;
	}


	public static Object postFile(String url,
								  ICommunicationsConfigProvider provider,
								  File file,
								  Object userObject,
								  IMediaPostObserver observer,
								  String resourceUri,
								  boolean published
	)
	{
		return postFile(url, provider, file, userObject, observer, resourceUri, published, false, false);
	}


	public static Object postFile(String url,
								  ICommunicationsConfigProvider provider,
								  File file,
								  Object userObject,
								  IMediaPostObserver observer,
								  String resourceUri,
								  boolean published,
								  boolean avatar
	)
	{
		return postFile(url, provider, file, userObject, observer, resourceUri, published, avatar, false);
	}


	public static Object postFile(String url,
								  ICommunicationsConfigProvider provider,
								  File file,
								  Object userObject,
								  IMediaPostObserver observer,
								  String resourceUri,
								  boolean published,
								  boolean avatar,
								  boolean synchronous
	)
	{

		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		try
		{

			Uploader up = new Uploader();

			HttpPost post = new HttpPost(url);
			up.post = post;

			setHeaders(post, provider);

			FileUploadThread thread = new FileUploadThread(observer, userObject, client, post, file, resourceUri, published, avatar);
			up.thread = thread;
			if (synchronous)
			{
				thread.run();
			} else
			{
				thread.start();
			}
			return up;
		} catch (Exception e)
		{

			e.printStackTrace();

		}
		return null;
	}

	public static void cancelFile(Object item)
	{


		if (item == null)
		{
			return;
		}

		try
		{
			if (HttpPost.class.isAssignableFrom(item.getClass()))
			{
				final HttpPost post = (HttpPost) item;

				Thread t = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						post.abort();
					}
				});
				t.start();
			} else
			{
				final Uploader up = (Uploader) item;
				Thread t = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						up.post.abort();
					}
				});
				t.start();

			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/*


	public static void getFile(String url, String destination, int id, IFileDownloadObserver observer)
	{
		try
		{
			URL durl = new URL(url);

			FileDownloadThread thread = new FileDownloadThread(observer, id, durl, destination);
			thread.start();

		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause());
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
		}

	}

*/
	public static void getImage(String url, String destination, int id, IImageDownloadObserver observer, int width, int height)
	{
		try
		{
			URL durl = new URL(url);
			ImageDownloadThread thread = new ImageDownloadThread(observer, id, durl, destination, width, height);
			thread.start();

		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause());
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
		}

	}


	public static void getArray(String url, ICommunicationsConfigProvider provider, int id, ICommunicationArrayObserver observer)
	{
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		try
		{

			HttpGet get = new HttpGet(url);
			setHeaders(get, provider);

			CommunicationsThreadArray thread = new CommunicationsThreadArray(observer, id, client, get, false);
			thread.start();

		} catch (Exception ex)
		{

			if (observer != null)
			{
				observer.onFail(id, "" + ex.getMessage() + ":" + ex.getCause());
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
			ex.printStackTrace();
		}

	}

	public static void getImage(String url, int id, IImageDownloadObserver observer, int width, int height)
	{
		getImage(url, null, id, observer, width, height);
	}

	public static void getData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer)
	{
		getData(url, provider, id, observer, false);
	}

	public static class NotifyErrorThread extends Thread
	{
		Exception _ex;
		ICommunicationObserver _observer;
		int _id;

		public NotifyErrorThread(int id, Exception e, ICommunicationObserver observer)
		{
			_ex = e;
			_observer = observer;
			_id = id;
		}

		/**
		 * Calls the <code>run()</code> method of the Runnable object the receiver
		 * holds. If no Runnable is set, does nothing.
		 *
		 * @see Thread#start
		 */
		@Override
		public void run()
		{
			super.run();

			try
			{
				if (_observer != null)
				{

					_observer.onFail(_id, "" + _ex.getMessage() + ":" + _ex.getCause(),null);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void getData(String url, ICommunicationsConfigProvider provider, int id, ICommunicationObserver observer, boolean plain)
	{

		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), CONNECTION_TIMEOUT);
		HttpResponse response;
		try
		{

			HttpGet get = new HttpGet(url);
			setHeaders(get, provider);

			CommunicationsThread thread = new CommunicationsThread(observer, id, client, get, plain, null);
			thread.start();

		} catch (Exception ex)
		{

			if (observer != null)
			{

				NotifyErrorThread t = new NotifyErrorThread(id, ex, observer);
				t.start();

				//observer.onFail(id,""+ex.getMessage()+":"+ex.getCause());
			}
			Log.e(TAG, "Error getting data:" + ex.getMessage());
			ex.printStackTrace();
		}



            /*
            HttpOptions httpoptions=new HttpOptions("http://localhost:" + PORT + "/antest/unannotatedPost");
            httpoptions.addHeader("Origin","http://in.org");
            httpoptions.addHeader("Content-Type","application/json");
            httpoptions.addHeader(CorsHeaderConstants.HEADER_AC_REQUEST_METHOD,"POST");
            httpoptions.addHeader(CorsHeaderConstants.HEADER_AC_REQUEST_HEADERS,"X-custom-1");
            HttpResponse response=client.execute(httpoptions);

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

                */




            /*

            SharedPreferences userDetails = context.getSharedPreferences("userdetails", MODE_PRIVATE);
Editor edit = userDetails.edit();
edit.clear();
edit.putString("username", txtUname.getText().toString().trim());
edit.putString("password", txtPass.getText().toString().trim());
edit.commit();
Toast.makeText(context, "Login details are saved..", 3000).show();

SharedPreferences userDetails = context.getSharedPreferences("userdetails", MODE_PRIVATE);
String Uname = userDetails.getString("username", "");
String pass = userDetails.getString("password", "");

             */

	}


	public static void Test()
	{
      /*
        assert (_context != null); //you shold set context calling init

        TestClass listener  = new TestClass();

        getData("https://onpublico.com/api/v1/home/news/",1,listener);



        /*

        SharedPreferences prefs = _context.getSharedPreferences("urls", Context.MODE_PRIVATE);

        //_context.getSharedPreferences()

        String str =  prefs.getString("string", "");

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("boolean", true);
        editor.putFloat("float", 1.3f);
        editor.putInt("int",2);
        editor.putLong("long", -1);
        editor.putString("string", "string");
        Set<String> stringSet = new HashSet<String>();
        stringSet.add("pepe");
        stringSet.add("juang");
        editor.putStringSet("stringset", stringSet);
        editor.commit();*/
	}

/*
    private static class TestClass implements ICommunicationObserver
    {



        @Override
        public void onSuccess(int operationId, JSONObject data) {
            Log.i(TAG,"operation Ok");

            if( data.has("objects"))
            {
                try {

                    JSONArray array =  data.getJSONArray("objects");
                    for (int i = 0; i < array.length();i++)
                    {
                        NewsList.news(array.getJSONObject(i));
                    }
                    Collection<News> news = NewsList.newsList();
                    Collection<Author> authors = Authors.authors();
                    assert( authors.isEmpty());
                    assert (!news.isEmpty());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFail(int operationId, String reason) {

            Log.w(TAG,"Operation failed: "+reason);
        }
    }
*/


	//==============================================================================================
	//                  Communications Thread
	//==============================================================================================

	/**
	 * Communications Threads manages Asynchronous http operations.
	 */
	public static class CommunicationsThread extends Thread
	{


		private static final String TAG = "OP:ComThread:";


		/**
		 * Observer to be called, it could be null
		 */
		private ICommunicationObserver _observer;

		/**
		 * Operation id
		 */
		private int _id;

		/**
		 * Request to be performed. should not be null
		 */
		private HttpUriRequest _request;

		/**
		 * Client to perform requests, should not be null
		 */
		private HttpClient _client;

		/**
		 * If response is plain instead of json ( oauth )
		 */
		private boolean _plain = false;


		private  PendingCommunication _pending = null;
		//TODO: usar volley!!!!!

		/**
		 * Constructor
		 *  @param observer Observer to be notified when operations ends. could be null
		 * @param id       Operation id, passed to observer.
		 * @param client   Client to perform operation, should not be null
		 * @param request  Request to be performed, should not be null
		 * @param pending
		 */
		public CommunicationsThread(ICommunicationObserver observer, int id,
									HttpClient client,
									HttpUriRequest request, boolean plain, PendingCommunication pending)
		{
			assert (request != null);
			assert (client != null);
			_observer = observer;
			_id = id;
			_client = client;
			_request = request;
			_plain = plain;
			_pending = pending;
		}


		/**
		 * Performs the operation.
		 */
		@Override
		public void run()
		{
			try
			{
				Date d = new Date();
				//execute request
				HttpResponse response = _client.execute(_request);
				Date now = new Date();
				int cd = 0;
				if (response != null)
				{
					cd = response.getStatusLine().getStatusCode();
				}

				long elapsed = (now.getTime() - d.getTime());
				Log.i(TAG, "T:" + elapsed + " for :" + _request.getURI() + " C:" + cd);
				//check response
				if (response != null)
				{

					if (_observer != null)
					{
						int code = response.getStatusLine().getStatusCode();
						if (code >= 200 && code < 300)
						{

							String result = null;
							if (response.getEntity() != null)
							{
								try
								{
									d = new Date();

									result = EntityUtilsHC4.toString(response.getEntity(), "utf-8");
									now = new Date();
									Log.i(TAG, "T:" + (now.getTime() - d.getTime()) + " for reading");

								} catch (Exception e)
								{
									e.printStackTrace();

									//InputStream in = response.getEntity().getContent(); //put returned data

									//result = convertStreamToString(in);
								}
							}


							JSONObject resultObject;
							if (_plain)
							{
								resultObject = new JSONObject();
								for (String part : result.split("&")) //'\u0026'.
								{
									String[] kv = part.split("=");
									if (kv.length == 2)
									{
										resultObject.put(kv[0], kv[1]);
									}
								}

							} else
							{


								if (result != null && result.length() > 3)
								{
									try
									{
										d = new Date();
										resultObject = new JSONObject(result);
										now = new Date();
										Log.i(TAG, "T:" + (now.getTime() - d.getTime()) + " for json parsing");
									}
									catch (Exception e)
									{
										if(_observer != null)
										{
											_observer.onFail(_id,"BAD JSON RESPONSE:("+result+")"+e.getMessage(),_pending);
										}
										e.printStackTrace();
										resultObject = new JSONObject();
									}
								} else
								{
									resultObject = new JSONObject();
								}
							}

							Log.i("ccc", "received ");
							_observer.onSuccess(_id, resultObject);
						} else //code not ok
						{
							String result = "";
							if (response.getEntity() != null)
							{
								InputStream in = response.getEntity().getContent();
								result = convertStreamToString(in);
							}

							try
							{
								if (code == 401)
								{
									result += " T:" + elapsed + " for :" + _request.getMethod() + " " + _request.getURI() + " \nH:";

									for (Header header : _request.getAllHeaders()
											)
									{
										result += "(" + header.getName() + "," + header.getValue() + ")";
									}
								}
							} catch (Exception e)
							{
								e.printStackTrace();
							}

							_observer.onFail(_id, "" + code + ":" + result,_pending);

							printHeaders(_request.getAllHeaders());
						}
					}
				} else
				{
					if (_observer != null)
					{
						_observer.onFail(_id, "No response",_pending);
					}
				}
			} catch (Exception ex)
			{
				if (_observer != null)
				{
					_observer.onFail(_id, "" + ex.getMessage() + ":" + ex.getCause(),_pending);
				}
				Log.e(TAG, "Error running thread:" + ex.getMessage());
				ex.printStackTrace();
			}

		}

		private void printHeaders(Header[] allHeaders)
		{
			//if()
			{
				String output = "Headers[";
				for (Header header : allHeaders
						)
				{
					output += "(" + header.getName() + "," + header.getValue() + ")";
				}
				Log.w(TAG, output + "]");
			}
		}
	}


	//==============================================================================================
	//                  Communications Thread for Array
	//==============================================================================================

	/**
	 * Communications Threads manages Asynchronous http operations.
	 */
	public static class CommunicationsThreadArray extends Thread
	{


		private static final String TAG = "OP:ComThread:";


		/**
		 * Observer to be called, it could be null
		 */
		private ICommunicationArrayObserver _observer;

		/**
		 * Operation id
		 */
		private int _id;

		/**
		 * Request to be performed. should not be null
		 */
		private HttpUriRequest _request;

		/**
		 * Client to perform requests, should not be null
		 */
		private HttpClient _client;

		/**
		 * If response is plain instead of json ( oauth )
		 */
		private boolean _plain = false;


		/**
		 * Constructor
		 *
		 * @param observer Observer to be notified when operations ends. could be null
		 * @param id       Operation id, passed to observer.
		 * @param client   Client to perform operation, should not be null
		 * @param request  Request to be performed, should not be null
		 */
		public CommunicationsThreadArray(ICommunicationArrayObserver observer, int id,
										 HttpClient client,
										 HttpUriRequest request, boolean plain)
		{
			assert (request != null);
			assert (client != null);
			_observer = observer;
			_id = id;
			_client = client;
			_request = request;
			_plain = plain;

		}


		/**
		 * Performs the operation.
		 */
		@Override
		public void run()
		{
			try
			{
				Date d = new Date();
				//execute request
				HttpResponse response = _client.execute(_request);
				Date now = new Date();
				int cd = 0;
				if (response != null)
				{
					cd = response.getStatusLine().getStatusCode();
				}
				Log.i(TAG, "T:" + (now.getTime() - d.getTime()) + " for :" + _request.getURI() + " C:" + cd);
				//check response
				if (response != null)
				{

					if (_observer != null)
					{
						int code = response.getStatusLine().getStatusCode();
						if (code >= 200 && code < 300)
						{

							String result = null;
							if (response.getEntity() != null)
							{

								try
								{
									result = EntityUtilsHC4.toString(response.getEntity(), "utf-8");
								} catch (Exception e)
								{
									e.printStackTrace();
								}
								//InputStream in = response.getEntity().getContent(); //put returned data

								//result = convertStreamToString(in);
							}


							JSONArray resultObject;

							{
								if (result != null && result.length() > 3)
								{
									resultObject = new JSONArray(result);
								} else
								{
									resultObject = new JSONArray();
								}
							}

							Log.i("ccc", "received ");
							_observer.onSuccess(_id, resultObject);
						} else //code not ok
						{
							String result = "";
							if (response.getEntity() != null)
							{
								InputStream in = response.getEntity().getContent();
								result = convertStreamToString(in);
							}

							_observer.onFail(_id, "" + code + ":" + result);
						}
					}
				} else
				{
					if (_observer != null)
					{
						_observer.onFail(_id, "No response");
					}
				}
			} catch (Exception ex)
			{
				if (_observer != null)
				{
					_observer.onFail(_id, "" + ex.getMessage() + ":" + ex.getCause());
				}
				Log.e(TAG, "Error running thread:" + ex.getMessage());
				ex.printStackTrace();
			}

		}
	}


	public static class FileUploadThread extends Thread
	{


		private static final String TAG = "OP:FUThread:";


		/**
		 * Observer to be called, it could be null
		 */
		private IMediaPostObserver _observer;

		/**
		 * Operation id
		 */
		private Object _userObject;

		/**
		 * Request to be performed. should not be null
		 */
		private HttpPost _request;

		/**
		 * Client to perform requests, should not be null
		 */
		private HttpClient _client;

		private File _file;

		private String _resourceUri;

		private boolean _published;

		private boolean _avatar;


		/**
		 * Constructor
		 *
		 * @param observer   Observer to be notified when operations ends. could be null
		 * @param userObject object to be passed to the observer.
		 * @param client     Client to perform operation, should not be null
		 * @param request    Request to be performed, should not be null
		 * @param published
		 */
		public FileUploadThread(IMediaPostObserver observer, Object userObject,
								HttpClient client,
								HttpPost request, File file, String resourceUri, boolean published,
								boolean avatar
		)
		{
			assert (request != null);
			assert (client != null);
			_observer = observer;
			_userObject = userObject;
			_client = client;
			_request = request;
			_file = file;
			_resourceUri = resourceUri;
			_published = published;
			_avatar = avatar;

		}


		/**
		 * Performs the operation.
		 */
		@Override
		public void run()
		{
			try
			{


				final File file = _file;
				//FileBody fb = new FileBody(file,"image/jpeg");

				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//.BROWSER_COMPATIBLE);


				builder.addPart("Title", new StringBody("Title"));
				builder.addBinaryBody(_avatar ? "avatar" : "file", file);//builder.addPart("file", fb);

				if (_resourceUri != null && _resourceUri.length() > 2)
				{
					builder.addPart("news", new StringBody(_resourceUri));

				}

				if (!_published)
				{

					Integer i = 0;
					builder.addPart("published", new StringBody("false"));
				}

				//builder.setBoundary("fkldjsalkfjdslka");
				final HttpEntity yourEntity = builder.build();


				class ProgressiveEntity implements HttpEntity
				{
					@Override
					public void consumeContent() throws IOException
					{
						yourEntity.consumeContent();
					}

					@Override
					public InputStream getContent() throws IOException,
							IllegalStateException
					{
						return yourEntity.getContent();
					}

					@Override
					public Header getContentEncoding()
					{
						return yourEntity.getContentEncoding();
					}

					@Override
					public long getContentLength()
					{
						return yourEntity.getContentLength();
					}

					@Override
					public Header getContentType()
					{
						return yourEntity.getContentType();
					}

					@Override
					public boolean isChunked()
					{
						return yourEntity.isChunked();
					}

					@Override
					public boolean isRepeatable()
					{
						return yourEntity.isRepeatable();
					}

					@Override
					public boolean isStreaming()
					{
						return yourEntity.isStreaming();
					} // CONSIDER put a _real_ delegator into here!

					@Override
					public void writeTo(final OutputStream outStream) throws IOException
					{


						class ProxyOutputStream extends FilterOutputStream
						{

							public ProxyOutputStream(OutputStream proxy)
							{
								super(proxy);
							}

							@Override
							public void write(int idx) throws IOException
							{
								out.write(idx);
							}

							@Override
							public void write(byte[] bts) throws IOException
							{
								out.write(bts);
							}

							@Override
							public void write(byte[] bts, int st, int end) throws IOException
							{
								out.write(bts, st, end);
							}

							@Override
							public void flush() throws IOException
							{
								out.flush();
							}

							@Override
							public void close() throws IOException
							{
								out.close();
							}
						} // CONSIDER import this class (and risk more Jar File Hell)

						class ProgressiveOutputStream extends ProxyOutputStream
						{

							long _count = 0;

							public ProgressiveOutputStream(OutputStream proxy)
							{
								super(proxy);


							}

							@Override
							public void write(byte[] buffer, int offset, int count) throws IOException
							{

								_count += count;
								if (_observer != null)
								{
									try
									{

										//calculate percentage:
										long length = file.length();
										if (length > 0)
										{
											float percentage = 100.0f * _count / length;
											_observer.OnProgress(percentage, _userObject);

										}
									} catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								//Log.i("uploading: " , "o "+offset +"c "+count);
								out.write(buffer, offset, count);
							}


						}
						yourEntity.writeTo(new ProgressiveOutputStream(outStream));
					}

				}
				;
				ProgressiveEntity myEntity = new ProgressiveEntity();


				_request.setEntity(myEntity);

				_request.removeHeaders("Content-Type");
				_request.addHeader(yourEntity.getContentType());

				//execute request
				HttpResponse response = _client.execute(_request);
				//check response
				if (response != null)
				{

					if (_observer != null)
					{
						int code = response.getStatusLine().getStatusCode();
						if (code >= 200 && code < 300)
						{


							/*
							InputStream in = response.getEntity().getContent(); //put returned data
							String result = null;
							result = convertStreamToString(in);
							Log.i(TAG, "Result: " + result);
*/
							JSONObject resultObject;


							resultObject = new JSONObject();

							Header headers[] = response.getHeaders("Location");
							if (headers != null && headers.length > 0)
							{
								try
								{

									String uri = headers[0].getValue();
									int st = uri.indexOf("/api");
									if (st > 0)
									{
										uri = uri.substring(st);
									}
									resultObject.put("resource_uri", uri);
									int end = uri.length() - 1;
									int start = uri.substring(0, end).lastIndexOf("/") + 1;

									resultObject.put("code", uri.substring(start, end));
									int id = Integer.parseInt(uri.substring(start, end));
									resultObject.put("id", id);

								} catch (Exception e)
								{
									e.printStackTrace();
								}
							}


							_observer.OnFinishedOk(resultObject, _userObject);
						}//code 2xx
						else
						{
							String result = "";
							InputStream in = response.getEntity().getContent();
							result = convertStreamToString(in);

							_observer.OnMediaFailed("" + code + ":" + result, _userObject);
						}
					}
				} else
				{
					if (_observer != null)
					{
						_observer.OnMediaFailed("no response", _userObject);
					}
				}
			} catch (Exception ex)
			{
				if (_observer != null)
				{
					_observer.OnMediaFailed("e:" + ex.getMessage(), _userObject);
				}
				Log.e(TAG, "Error running thread:" + ex.getMessage());
				ex.printStackTrace();
			} finally
			{
				_observer = null;
			}

		}
	}


	public interface IMediaPostObserver
	{
		public void OnFinishedOk(JSONObject data, Object userObject);

		public void OnMediaFailed(String reason, Object userObject);

		public void OnProgress(float percentage, Object userObject);

	}

	/**
	 * Location alerter to redirect user to configuration
	 */
	public interface ILocationAlerter
	{
		/**
		 * Alert the user that location is not enabled, good place to open device location settings
		 * for the user
		 *
		 * @param parent could be null.
		 */
		void alertNotEnabled(Activity parent);
	}

	/*
	static void runOnMainUIThread(Runnable runnable) {
		if (Looper.getMainLooper().getThread() == Thread.currentThread())
			runnable.run();
		else {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(runnable);
		}
	}
	*/

	public static void addGeoData(JSONObject object)
	{
		try
		{
			Communications.LatLong position = Communications.getCurrentGeolocation();
			if (position.latitude != 0 && position.longitude != 0)
			{
				JSONObject obj = new JSONObject();
				JSONArray array = new JSONArray();
				array.put(position.longitude);
				array.put(position.latitude);
				obj.put("type", "Point");
				obj.put("coordinates", array);
				object.put("position", obj);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		LogHelper.logConsole("GEO:", "addGeoData: " + object);
	}

	public static class PendingCommunication
	{

		public static final String RETRIES = "retries";
		public static final String ERROR = "error";
		public static final String BODY = "body";
		public static final String METHOD = "method";
		public static final String NAME = "name";
		public static final String VALUE = "value";
		public static final String HEADERS = "headers";
		public static final String URL ="url";
		public static final String TIMESTAMP = "timestamp";

		public enum Method
		{
			NONE,
			PUT,
			POST,
			GET,
			PATCH
		}
		public class JsonHeader implements Header
		{

			private String name;
			private String value;
			public JsonHeader( JSONObject object)
			{
				try
				{
					name = object.getString(NAME);
					value = object.getString(VALUE);
				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public String getName()
			{
				return name;
			}

			@Override
			public String getValue()
			{
				return value;
			}

			@Override
			public HeaderElement[] getElements() throws ParseException
			{
				return new HeaderElement[0];
			}
		}
		public String url;
		public int nRetries;
		public String errorStack;
		public Header[] headers;
		public String body;
		public Method method;
		public long timestamp;



		public PendingCommunication(Method method,String body, Header[] headers, String url )
		{
			nRetries =0;
			errorStack = "";
			this.method = method;
			this.body = body;
			this.headers = headers;
			this.url = url;
			this.timestamp = new Date().getTime();
		}

		public PendingCommunication(JSONObject object)
		{
			try
			{

				url = object.getString(URL);
				nRetries = object.getInt(RETRIES);
				errorStack = object.getString(ERROR);
				body = object.getString(BODY);
				method = Method.valueOf(object.getString(METHOD));
				timestamp = object.getLong(TIMESTAMP);
				JSONArray array = object.getJSONArray(HEADERS);
				if( array != null && array.length() > 0)
				{

					headers = new Header[array.length()];

					for( int i =0; i < array.length(); i++)
					{

						headers[i]= new JsonHeader(array.getJSONObject(i));
					}
				}
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		public JSONObject serialize()
		{
			JSONObject object = new JSONObject();
			try
			{
				object.put(URL,url);
				object.put(RETRIES,nRetries);
				object.put(ERROR, errorStack);
				object.put(BODY, body);
				object.put(METHOD, method.name());
				object.put(TIMESTAMP, timestamp);
				JSONArray array = new JSONArray();
				for(Header h : headers)
				{
					JSONObject jh = new JSONObject();
					jh.put(NAME, h.getName());
					jh.put(VALUE, h.getValue());
					array.put(jh);
				}
				object.put(HEADERS,array);
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
			return object;
		}
	}
}
