package dev.brendenson.flutter_location;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationRequest;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;

public class ClientHandler implements MethodCallHandler {
    private final String TAG = ClientHandler.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_CODE = 34;

    private static Context mContext;
    private static BinaryMessenger mMessenger;
    private static LocationService mService = null;
    private static EventHandler mEvent = null;

    private static boolean mBound = false;

    private static HashMap<Integer, Integer> serviceAccuracy = new HashMap<Integer, Integer>() {
        {
            put(0, LocationRequest.PRIORITY_NO_POWER);
            put(1, LocationRequest.PRIORITY_LOW_POWER);
            put(2, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            put(3, LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    };

    private static final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    public static void onStartListening() {
        if (mContext != null) {
            mContext.bindService(new Intent(mContext, LocationService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    public static void onStopListening() {
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    public ClientHandler(BinaryMessenger messenger) {
        this.mMessenger = messenger;
    }

    public void registerActivity(Context context) {
        this.mContext = context;

        this.mEvent = new EventHandler(context, this.mMessenger);

        if (EventController.requestingLocationUpdates(this.mContext)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }

    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "hasPermissions":
                result.success(checkPermissions() ? 1 : 0);
                break;
            case "requestPermissions":
                requestPermissions();
                result.success(1); // to-do: check user granted permissions
                break;
            case "startService":
                startLocationService(call, result);
                break;
            case "stopService":
                stopLocationService();
                result.success(1);
                break;
            case "getPosition":
                getPosition(result);
                break;
            case "distanceBetween":
                distanceBetween(call, result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void startLocationService(MethodCall call, MethodChannel.Result result) {
        if (!checkPermissions()) {
            requestPermissions();
            result.success(0);
            return;
        }

        try {
            final String channelName = call.argument("notifChannel");
            final String notifTitle = call.argument("notifTitle");
            final String notifContent = call.argument("notifContent");
            final String iconResName = call.argument("notifIconResName");
            final String iconResPackage = call.argument("notifIconResPackage");
            final Integer accurary = serviceAccuracy.get(call.argument("accuracy"));
            final Long updateInterval = new Long((int) call.argument("interval"));
            final Long fastestUpdateInterval = updateInterval / 2;
            final Float distance = new Float((double) call.argument("distance"));

            mService.changeSettings(channelName, notifTitle, notifContent, iconResName,
                    iconResPackage, accurary, updateInterval, fastestUpdateInterval, distance);

            mService.requestLocationUpdates();

            result.success(1);
        } catch (Exception e) {
            result.success(0);
        }
    }

    private void stopLocationService() {
        mService.removeLocationUpdates();
    }

    private void getPosition(MethodChannel.Result result) {
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }

        mService.singleLocation = result;
        mService.requestLocationUpdates();
    }

    private void distanceBetween(MethodCall call, MethodChannel.Result result) {
        float[] results = new float[1];

        try {
            Location.distanceBetween(
                    (double) call.argument("startLatitude"),
                    (double) call.argument("startLongitude"),
                    (double) call.argument("endLatitude"),
                    (double) call.argument("endLongitude"),
                    results);

            result.success(results[0]);
        } catch (IllegalArgumentException e) {
            result.error(TAG,
                    "Invalid coordinates",
                    null);
        }
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
        } else {
            Log.i(TAG, "Requesting permission");

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
        }
    }
}
