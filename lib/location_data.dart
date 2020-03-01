class LocationData {
  final double latitude;
  final double longitude;
  final double timestamp;

  LocationData._(this.latitude, this.longitude, this.timestamp);

  factory LocationData.fromMap(Map<String, double> dataMap) {
    return LocationData._(
      dataMap['latitude'],
      dataMap['longitude'],
      dataMap['timestamp'],
    );
  }

  @override
  String toString() {
    return "Location <latitude: $latitude, longitude: $longitude>";
  }
}