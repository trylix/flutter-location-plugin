package dev.brendenson.flutter_location;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.HashMap;

public class EventController {
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    public static HashMap createLocationData(Location location) {
        HashMap<String, Double> response = new HashMap<>();

        response.put("latitude", location.getLatitude());
        response.put("longitude", location.getLongitude());
        response.put("timestamp", (double)location.getTime());

        if (location.hasAltitude())
            response.put("altitude", location.getAltitude());

        if (location.hasAccuracy())
            response.put("accuracy", (double) location.getAccuracy());

        if (location.hasBearing())
            response.put("heading", (double) location.getBearing());

        if (location.hasSpeed())
            response.put("speed", (double) location.getSpeed());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasSpeedAccuracy())
            response.put("speed_accuracy", (double) location.getSpeedAccuracyMetersPerSecond());

        return  response;
    }
}
