package com.tendarts.sdk.common;



/**
 * Created by jorgearimany on 5/4/17.
 */

public class Constants extends ConstantsBase
{
	public static final String devices =baseUrl+ "/api/v1/devices/";
	public static final String device =baseUrl+ "/api/v1/devices/%s/";
	public static final String deviceReference = "/api/v1/devices/%s/";
	public static final String deviceAccess = baseUrl+"/api/v1/devices/%s/access/";
	public static final String disablePush= baseUrl+"/api/v2/devices/disable/";

	public static final String pushReceived = baseUrl +"/api/v1/pushes/%s/received/";
	public static final String pushRead = baseUrl+"/api/v1/pushes/%s/read/";
	public static final String pushAllRead = baseUrl +"/api/v1/pushes/all_read/";
	public static final String pushClicked = baseUrl +"/api/v1/pushes/%s/follow/";



	public static final String geostats = baseUrl+"/api/v1/devices/%s/";

	public static final String registerUser = baseUrl+"/api/v1/personas/";
	public static final String user = baseUrl+"/api/v1/personas/%s/";
	public static final String relativeUser="/api/v1/personas/%s/";
	public static final String links = baseUrl+"/api/v1/devices/links/";
}
