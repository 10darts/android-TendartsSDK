package com.tendarts.sdk.geo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tendarts.sdk.TendartsSDK;
import com.tendarts.sdk.client.TendartsClient;
import com.tendarts.sdk.common.LogHelper;
import com.tendarts.sdk.communications.Communications;

import java.lang.ref.WeakReference;

/**
 * Created by jorgearimany on 6/4/17.
 */

public class GoogleUpdates implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

	GoogleApiClient mGoogleApiClient;
	LocationRequest mLocationRequest;
	WeakReference<Activity> activityRef;

	private static final String TAG ="GU";

	public GoogleUpdates(Context context, Activity activity) {

		activityRef = new WeakReference<Activity>(activity);
		// Create an auto-managed GoogleApiClient with acccess to App Invites.
		mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
				//.addApi(AppInvite.API)
				.addApi(LocationServices.API)
				//.enableAutoManage(context, this)
				.addConnectionCallbacks(this)
				.build();
	}


	public void onStart() {
		try {
			LogHelper.logConsole(TAG, "onStart: ");
			if (mGoogleApiClient != null) {
				mGoogleApiClient.connect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onStop() {
		try {
			LogHelper.logConsole(TAG, "onStop: ");
			if (mGoogleApiClient != null) {
				mGoogleApiClient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onResume() {
		try {
			LogHelper.logConsole(TAG, "onResume: ");
			if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
				startLocationUpdates();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		LogHelper.logConsole(TAG, "onLocationChanged: ");
		if (location != null) {
			//Log.i(TAG, "New location:" + location+" p:"+location.getProvider()+" acc:"+location.getAccuracy());
			float acc = location.getAccuracy();
			Communications.lastProvider = location.getProvider();
			Communications.lastSource = "Service";
			Communications.lastPrecission = acc;
			if (acc !=0 && acc < Communications.MIN_ACCURACY) {
				Communications.outOfRange = false;
				Communications.setGeolocation(location.getLatitude(), location.getLongitude());
			} else {
				Communications.outOfRange = true;
			}
			TendartsSDK.onNewLocation();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		LogHelper.logConsole(TAG, "onConnectionSuspended: "+i);
	}

	@Override
	public void onConnected(Bundle bundle) {
		LogHelper.logConsole(TAG, "onConnected: ");
		startLocationUpdates();
	}

	public void startLocationUpdates() {
		startLocationUpdates(true);
	}

	public void startLocationUpdates(boolean askForPermissions) {
		getLocationUpdates(askForPermissions,true);
	}

	public boolean asking = false;

	public void getLocationUpdates(boolean askForPermissions, boolean start) {

		try {
			LogHelper.logConsole(TAG, "getLocationUpdates, ask:"+askForPermissions+", start:"+start);

			Location loc = LocationServices.FusedLocationApi.getLastLocation(
					mGoogleApiClient);
			if (loc != null) {

				//Log.i(TAG, "OnConnected, new location:" + loc +" p:"+loc.getProvider() +" acc:"+loc.getAccuracy());
				float acc = loc.getAccuracy();
				Communications.lastProvider = loc.getProvider();
				Communications.lastSource = "LastLocation";
				Communications.lastPrecission = acc;
				if (acc != 0 && acc < Communications.MIN_ACCURACY) {
					Communications.outOfRange = false;
					Communications.setGeolocation(loc.getLatitude(), loc.getLongitude());
				} else {
					Communications.outOfRange = true;
				}
				TendartsSDK.onNewLocation();
			}


			//configure location request
			if( mLocationRequest == null) {
				mLocationRequest = new LocationRequest();
				mLocationRequest.setInterval(120000);
				mLocationRequest.setFastestInterval(20000);
				mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		} catch ( Exception e) {
			e.printStackTrace();
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && askForPermissions && ! asking) {
			Activity mainActivity = null;
			if (activityRef != null) {
				mainActivity = activityRef.get();
			}
			final Activity activity = mainActivity;

			assert activity != null;

			try {
				asking = true;
				// Log.d(TAG, "location in v6");
				if (ContextCompat.checkSelfPermission(mainActivity,
						android.Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED

						&& activity != null
						) {

					//Log.d(TAG, "location in v6 not granted");
					// Should we show an explanation?
					if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
							android.Manifest.permission.ACCESS_FINE_LOCATION)) {

						// Show an expanation to the user *asynchronously* -- don't block
						// this thread waiting for the user's response! After the user
						// sees the explanation, try again to request the permission.

						//Log.d(TAG, "location in v6 should inform");
						AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);


						builder.setMessage(TendartsClient.instance(mainActivity.getApplicationContext()).getLocationExplanation(mainActivity.getApplicationContext()));
// Add the buttons
						builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{

								//Log.d(TAG, "location in v6 requesting again");
								ActivityCompat.requestPermissions(activity,
										new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
										TendartsSDK.REQUEST_LOCATION);
								asking = false;
							}
						});

						builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								//Log.d(TAG, "location in v6 user cancelled");
								asking = false;
								dialog.dismiss();
								TendartsClient.instance(activity).onUserRejectedLocationPermission();
							}
						});

						// Create the AlertDialog
						AlertDialog dialog = builder.create();
						dialog.show();


					} else {

						// No explanation needed, we can request the permission.
						//Log.d(TAG, "location in v6 requesting without explanation");
						asking = true;
						ActivityCompat.requestPermissions(mainActivity,
								new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
								TendartsSDK.REQUEST_LOCATION);
					}

				} else {
					asking = false;
					//Log.d(TAG, "location in v6: permission already granted");
				}
			} catch (Exception e) {
				e.printStackTrace();
				asking = false;
			}
		}

		if (start) {

			try {
				LocationServices.FusedLocationApi.requestLocationUpdates(
						mGoogleApiClient, mLocationRequest, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		LogHelper.logConsole(TAG, "onConnectionFailed: "+connectionResult);
		Log.w(TAG, "google connection failed:" + connectionResult.getErrorMessage());
	}

	public void onPause() {
		try {
			if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
				LocationServices.FusedLocationApi.removeLocationUpdates(
						mGoogleApiClient, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDestroy() {
		LogHelper.logConsole(TAG, "onDestroy: ");
		if (mGoogleApiClient != null) {
			try {

				mGoogleApiClient.unregisterConnectionCallbacks(this);
				mGoogleApiClient.unregisterConnectionFailedListener(this);

				if (mGoogleApiClient.isConnected()) {
					LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
					mGoogleApiClient.disconnect();
				}

				//AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, null, true).setResultCallback(null);
				mGoogleApiClient = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
