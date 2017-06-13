# AndroidSDK

# QuickStart

The following instructions will guide you through the process of implementing 10 Darts SDK in your application, the instructions are based on _Android Studio._

If you are not familiar with _Android Studio_ please refer to this [Quick tutorial](doc/app.md).
 
Before proceeding you should have created a GCM project in your firebase console, please refer to [Google Cloud Messaging](doc/gcm.md) an follow the steps.

Note that the target devices should have Google Play services installed and updated on it for the proper operation of this SDK

## 1 Configure the Application
### Library
* Copy the SDK aar file to your libs directory
### Gradle
* Add the library to the dependencies:

    `compile(name: 'sdk-sdkProduction-release', ext: ‘aar')`

* Add the needed dependencies:

~~~
compile 'com.google.android.gms:play-services-auth:9.8.0'
 compile 'com.google.android.gms:play-services-gcm:9.8.0'
 compile 'com.google.android.gms:play-services-location:9.8.0' 
compile 'org.apache.httpcomponents:httpclient-android:4.3.5' 
compile 'org.apache.httpcomponents:httpmime:4.3.5'
~~~

### Manifest

* Add the required permissions:
~~~XML
<uses-permission android:name="android.permission.INTERNET"/>
 <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE"/>
 <uses-permission android:name="com.google.android.c2dm.permission.REGISTER"/> 
<uses-permission android:name="com.google.android.c2dm.permission.SEND"/> 
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> 
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 <uses-permission android:name="android.permission.GET_ACCOUNTS"/>
~~~

* Add C2D Message permission, note  you should change **yourpackagename** with your app package, e.g.: _com.yourcompany.appname_
~~~XML

<permission
 android:name="yourpackagename.C2D_MESSAGE"
 android:protectionLevel=“signature"/>

 
<uses-permission android:name="yourpackagename.C2D_MESSAGE"/>

~~~

* In Application section add the required receivers note  you should change **yourpackagename** with your app package, e.g.: _com.yourcompany.appname_:

~~~XML
<receiver android:name="com.darts.sdk.gcm.DartsReceiver">
    <intent-filter>
        <action android:name="com.darts.sdk.CLEAR_PUSHES"/>
        <action android:name="com.darts.sdk.OPEN_PUSH"/>
        <action android:name="com.darts.sdk.OPEN_LIST"/>
    </intent-filter>
</receiver>
    
<receiver
    android:name="com.google.android.gms.gcm.GcmReceiver"
    android:exported="true"
    android:permission="com.google.android.c2dm.permission.SEND" >
    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
        <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
        <category android:name="yourpackagename" />
    </intent-filter>
</receiver>
    
<service
    android:name="com.darts.sdk.gcm.GCMListenerService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
    </intent-filter>
</service>
    
<service
    android:name="com.darts.sdk.gcm.GCMInstanceIdListener"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.android.gms.iid.InstanceID"/>
    </intent-filter>
</service>
    
<service
    android:name="com.darts.sdk.gcm.GCMRegistrationIntentService"
    android:exported="false">
</service>
~~~

* The last step is to add your configuration inside the Application section
~~~XML
<meta-data
    android:name="darts_accessToken"
    android:value="value"/>
    
<meta-data
    android:name="darts_clientClass"
    android:value="value"/>
    
<meta-data
    android:name="darts_GcmSenderId"
    android:value=“value"/>
~~~

  * **darts_accessToken**: your access token to 10 Darts
  * **darts_clientClass**: the full qualified name of your SDK implementation (see [Implementing client class](doc/client.md))
  * **darts_GcmSenderId**: the sender id  you copied in the last step of  [Google Cloud Messaging](doc/gcm.md)


## 2 Implement client class
Follow the steps in [Implementing client class](doc/client.md) and you are done!.

## Next steps
You can also make your app Geolocation aware, for that, just follow [Adding Geolocation](doc/geolocation.md)



# Aditional Documentation

[SDKClient class documentation](src/main/java/com/darts/sdk/client/README.md)

[SDK class documentation](src/main/java/com/darts/sdk/README.md) 
