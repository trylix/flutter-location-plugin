# Flutter Location

A Flutter plugin which provides foreground and background access to the android platform location services. This plugin is under active development.

## Features
- Get the current location of the device;
- Continuous location updates with background support;
- Calculate the distance between two coordinates;
- Manual location permission management;
- Automatic location permission management;

#### Planned features:
- iOS platform support;
- Check location services are enabled;
- Get coordinates from an address;

## Getting started
To use this plugin, add ``flutter_location`` as a dependency in your ``pubspec.yaml`` file.

#### Plugin permissions and services

After importing this plugin to your project as usual, add the following to your ``AndroidManifest.xml`` within the ``<manifest></manifest>`` tags:

```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

Next, add the following lines on ``<application></application>`` tags:

```
<service
  android:name="dev.brendenson.flutter_location.LocationService"
  android:stopWithTask="true"
  android:foregroundServiceType="location"
  android:enabled="true"
  android:exported="true" />
```

Noew, import the package ``dev.brendenson.flutter_location.ClientHandlere`` in the ``MainActivity`` file and add the following lines:

```
  @Override
  protected void onStart() {
    super.onStart();

    ClientHandler.onStartListening();
  }

  @Override
  protected void onStop() {
    super.onStop();

    ClientHandler.onStopListening();
  }
```

##### AndroidX
This plugin is dependent AndroidX version of the Android Support Libraries. Detailed instructions can be found [here](https://flutter.dev/docs/development/packages-and-plugins/androidx-compatibility).

 1. Include the following settings to ``android/gradle.properties`` file:
 ```
android.useAndroidX=true
android.enableJetifier=true
 ```

 2. Update you ``compileSdkVersion`` in your ``android/app/build.gradle`` file to 29:
```
 android {
 compileSdkVersion 29
 ...
}
 ```

3. Make sure you replace all the android. dependencies to their AndroidX counterparts [a full list can be found here](https://developer.android.com/jetpack/androidx/migrate).

## Cookbooks

#### Create a location service
```
FlutterLocation location - FlutterLocation();

location.startService(
  String notifChannel,
  String notifTitle,
  String notifContent,
  String notifIconResPackage,
  String notifIconResName,
  LocationAccuracy accuracy,
  long interval,
  double distance
);
```

*Methods summary*

- **notifChannel** *String* - The user visible name of the notification channel.

- **notifTitle** *String* - Set the title of the notification.
- **notifContent** *String* - Set the text of the notification.
- **notifIconResPackage** *String* - Resource type to find.
- **notifIconResName** *String* - The name of the desired resource.
- **accuracy** *LocationAccuracy* - Set the priority of the request.
- **interval** *Long* - Set the desired interval for active location updates, in milliseconds.
- **distance** *Float* - Set the minimum displacement between location updates in meters

*Returns*
- **Future\<bool>** - Service status opened

#### Stop location service

```
FlutterLocation location - FlutterLocation();

location.stopService();
```

*Returns* 
- **Future\<bool>** - Service is stopped

#### Request location updates
```
FlutterLocation location = FlutterLocation();

location.requestLocationUpdates().listen((LocationData data) {
  //your callback function
},
onError: () {
  //your error function
});
```

*Returns*
- **Stream\<LocationData>** - Location latitude and longitude updates

#### Request current location
```
FlutterLocation location = FlutterLocation();

location.getLocation();
```

*Returns*
- **Stream\<LocationData>** - Current location latitude and longitude

#### Approximate distance in meters between two locations
```
FlutterLocation location = FlutterLocation();

location.distanceBetween(
    double startLatitude,
    double startLongitude,
    double endLatitude,
    double endLongitude
);
```

*Methods summary*

- **startLatitude** *double* - The starting latitude.

- **startLongitude** *double* - The starting longitude.
- **endLatitude** *double* - The ending latitude.
- **endLongitude** *double* - The ending longitude.

*Returns*
- **Future\<double>** - Approximate distance in meters.

## Contributing
Thanks for being interested on making this plugin better. We encourage everyone to help improving this project with some new features, bug fixes and performance issues.

## License
Distributed under an MIT license. See LICENSE file for more information.

## Author
Flutter location plugin is developed by Brendenson Andrade.
[LinkedIn](https://www.linkedin.com/in/dobrendenson/) | [GitHub](https://github.com/trylix) | [E-mail](mailto:brendensond@gmail.com)