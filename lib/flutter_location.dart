import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_location/location_accuracy.dart';
import 'package:flutter_location/location_data.dart';

class FlutterLocation {
  factory FlutterLocation() {
    if (_instance == null) {
      final MethodChannel methodChannel = MethodChannel('dev.brendenson.flutter_location/onRequest');
      final EventChannel eventChannel = EventChannel('dev.brendenson.flutter_location/onEvent');

      _instance = FlutterLocation.private(methodChannel, eventChannel);
    }

    return _instance;
  }

  @visibleForTesting
  FlutterLocation.private(this._methodChannel, this._eventChannel);

  static FlutterLocation _instance;

  final MethodChannel _methodChannel;
  final EventChannel _eventChannel;

  Stream<LocationData> _locationUpdates;

  Future<bool> startService({
    notifChannel = "Background location monitoring",
    notifTitle = "Background application",
    notifContent = "This application is running in background",
    notifIconResPackage = "mipmap",
    notifIconResName = "ic_launcher",
    LocationAccuracy accuracy = LocationAccuracy.PRIORITY_HIGH_ACCURACY,
    int interval = 5000,
    double distance = 0
  }) async => (
      await _methodChannel.invokeMethod('startService', {
        'notifChannel': notifChannel,
        "notifTitle": notifTitle,
        "notifContent": notifContent,
        'notifIconResPackage': notifIconResPackage,
        'notifIconResName': notifIconResName,
        "accuracy": accuracy.index,
        "interval": interval,
        "distance": distance
      }).then((data) => data == 1)
  );

  Future<bool> stopService() async => (
      await _methodChannel.invokeMethod('stopService').then((data) => data == 1)
  );

  Future<bool> hasPermissions() async => (
      await _methodChannel.invokeMethod('hasPermissions').then((data) => data == 1)
  );

  Future<bool> requestPermissions() async => (
      await _methodChannel.invokeMethod('requestPermissions').then((data) => data == 1)
  );

  Future<LocationData> getLocation() async {
    Map<String, double> resultMap = (await _methodChannel.invokeMethod('getLocation'))
        .cast<String, double>();

    return LocationData.fromMap(resultMap);
  }

  Stream<LocationData> requestLocationUpdates() {
    if (_locationUpdates == null) {
      _locationUpdates = _eventChannel .receiveBroadcastStream()
          .map<LocationData>(
              (data) => LocationData.fromMap(data.cast<String, double>())
      );
    }

    return _locationUpdates;
  }

  Future<double> distanceBetween({
    double startLatitude,
    double startLongitude,
    double endLatitude,
    double endLongitude
  }) =>
      _methodChannel.invokeMethod('distanceBetween', {
        'startLatitude': startLatitude,
        'startLongitude': startLongitude,
        'endLatitude': endLatitude,
        'endLongitude': endLongitude
      }).then<double>((dynamic result) => result);
}
