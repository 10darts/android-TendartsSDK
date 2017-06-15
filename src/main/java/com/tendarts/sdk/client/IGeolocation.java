package com.tendarts.sdk.client;

/**
 * Created by jorgearimany on 19/4/17.
 */

public interface IGeolocation
{
	/**
	 * Called when the user has rejected location permission, do not make any further UI interaction with the user regarding location permission
	 */
	void onUserRejectedLocationPermission();

	/**
	 * Called when the user has accepted location permission, note that asking for permissions depends on several factors and is not always asked to the user.
	 */
	void onUserAcceptedLocationPermission();
}
