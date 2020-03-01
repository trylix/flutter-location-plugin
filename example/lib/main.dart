import 'dart:async';

import 'package:flutter/material.dart';

import 'package:flutter_location/flutter_location.dart';
import 'package:flutter_location/location_data.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<LocationData> locations = new List();
  ScrollController controller = new ScrollController();

  bool isRunning = false;

  FlutterLocation location;

  @override
  void initState() {
    super.initState();
    initLocationService();
  }

  Future<void> initLocationService() async {
    location = FlutterLocation();

    location.requestLocationUpdates().listen(_onEvent, onError: _onError);
  }

  void _onEvent(LocationData locationData) {
    setState(() {
      print(locationData.toString());

      locations.add(locationData);

      controller.jumpTo(controller.position.maxScrollExtent);
    });
  }

  void _onError(Object error) {
    print(error);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: <Widget>[
              Column(
                children: <Widget>  [
                  Center(
                    child: RaisedButton(
                      child: Text(!isRunning
                          ? "Start service"
                          : "Stop service"
                      ),
                      onPressed: () => updateService(),
                    ),
                  ),
                  Padding(
                    padding: EdgeInsets.only(top: 10, bottom: 10),
                    child: Text(locations.length.toString()),
                  ),
                ],
              ),
              Expanded(
                flex: 1,
                child: _listLocations(),
              )
            ],
          ),
        ),
      ),
    );
  }

  Widget _listLocations() {
    return ListView.builder(
      controller: controller,
      itemCount: locations.length,
      itemBuilder: (context, index) {
        final _location = locations[index];
        return Card(
          child: ListTile(
            title: Text(_location.toString()),
          ),
        );
      },
    );
  }

  void updateService() {
    setState(() {
      if (isRunning) {
        location.stopService();
        isRunning = false;
        return;
      }

      location.hasPermissions().then((hasPermissions) {
        if (!hasPermissions) {
          location.requestPermissions();
        } else {
          location.startService().then((running) => isRunning = running);
        }
      });
    });
  }
 }
