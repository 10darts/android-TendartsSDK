# 10darts Android SDK

[![Join the chat at https://gitter.im/10darts/android-sdk](https://badges.gitter.im/10darts/android-sdk.svg)](https://gitter.im/10darts/android-sdk?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

#### Installation

See 10dart's [Android SDK Setup](http://docs.10darts.com/tutorials/android/setup.html) guide for documentation.

#### Support

If you have some feature requests or bug reports related with the Android SDK, you can use the
the [Github issue tracker](https://github.com/10darts/android-sdk/issues) from this repository,

### Common problems ###

If you have conflicts with Google Play Services version, you can exclude it from 10darts and add it manually with the version you need:

```
    implementation  ('com.10darts:sdk:1.26') {
        exclude group: 'com.google.android.gms', module: 'play-services-location'
        exclude group: 'com.google.android.gms', module: 'play-services-gcm'
    }

    implementation "com.google.android.gms:play-services-location:15.0.1"
    implementation ("com.google.android.gms:play-services-gcm:15.0.1")
```

Keep in mind that at least, it should be the version that we define [here](https://github.com/10darts/android-TendartsSDK/blob/development/build.gradle) to prevent problems related with missing API methods.
