package com.tendarts.sdk.common;



/**
 * Created by jorgearimany on 5/4/17.
 */

public class Constants extends ConstantsBase {

	public static final String DEVICES = BASE_URL + "/api/v1/devices/";
	public static final String DEVICE = BASE_URL + "/api/v1/devices/%s/";
	public static final String DEVICE_REFERENCE = "/api/v1/devices/%s/";
	public static final String DEVICE_ACCESS = BASE_URL +"/api/v1/devices/%s/access/";
	public static final String PUSH = BASE_URL +"/api/v1/pushes/%s/";
	public static final String SESSION_EVENT = "/api/v1/event_kinds/session/";
	public static final String EVENTS = BASE_URL + "/api/v1/events/";
	public static final String DISABLE_PUSH = BASE_URL +"/api/v2/devices/disable/";

	public static final String PUSH_RECEIVED = BASE_URL +"/api/v1/pushes/%s/received/";
	public static final String PUSH_APP_TIME = BASE_URL +"/api/v1/pushes/%s/received/";
	public static final String PUSH_READ = BASE_URL +"/api/v1/pushes/%s/read/";
	public static final String PUSH_ALL_READ = BASE_URL +"/api/v1/pushes/all_read/";
	public static final String PUSH_CLICKED = BASE_URL +"/api/v1/pushes/%s/follow/";

	public static final String KEYS_DEVICES = BASE_URL + "/api/v1/keys/devices/";
	public static final String KEYS_PERSONAS = BASE_URL + "/api/v1/keys/personas/";


	public static final String GEOSTATS = BASE_URL +"/api/v1/devices/%s/";

	public static final String REGISTER_USER = BASE_URL +"/api/v1/personas/";
	public static final String USER = BASE_URL +"/api/v1/personas/%s/";
	public static final String RELATIVE_USER = "/api/v1/personas/%s/";
	public static final String LINKS = BASE_URL +"/api/v1/devices/links/";

	public static final String REPLIES_SELECTED = BASE_URL + "/api/v1/replies/%s/selected/";

}
