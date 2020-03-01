package dev.brendenson.flutter_location;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import io.flutter.Log;
import io.flutter.plugin.common.MethodChannel;

public class LocationService extends Service {
    private static final String PACKAGE_NAME =
            "dev.brendenson.flutter_location";

    private static final String TAG = LocationService.class.getSimpleName();

    private static final String CHANNEL_ID = PACKAGE_NAME + ".notification";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";

    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    private static long UPDATE_INTERVAL_IN_MILLISECONDS = 250;
    private static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static Integer LOCATION_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private static Float DISTANCE_FILTER = 0f;

    private static final int NOTIFICATION_ID = 12323;
    private static String NOTIFICATION_CHANNEL_NAME = "Background location monitoring";
    private static String NOTIFICATION_TITLE = "Background application";
    private static String NOTIFICATION_CONTENT = "This application is running in background";
    private static int NOTIFICATION_ICON;

    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    private Location mLocation;

    public MethodChannel.Result singleLocation;

    private boolean hasNotified = false;

    public LocationService() {

    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationCallback();
        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        mServiceHandler = new Handler(handlerThread.getLooper());

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!mChangingConfiguration && EventController.requestingLocationUpdates(this)) {
            startForeground(NOTIFICATION_ID, getNotification());
        }

        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    public void requestLocationUpdates() {
        EventController.setRequestingLocationUpdates(this, true);

        startService(new Intent(getApplicationContext(), LocationService.class));

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            EventController.setRequestingLocationUpdates(this, false);

            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationUpdated(locationResult.getLastLocation());
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LOCATION_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISTANCE_FILTER);
    }

    public void removeLocationUpdates() {
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            EventController.setRequestingLocationUpdates(this, false);

            stopSelf();
        } catch (SecurityException unlikely) {
            EventController.setRequestingLocationUpdates(this,
                    true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    private Notification getNotification() {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent notificationIntent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent intent = new Intent(this, LocationService.class);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_CONTENT)
                .setTicker(NOTIFICATION_CONTENT)
                .setSmallIcon(NOTIFICATION_ICON)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onLocationUpdated(Location location) {
        mLocation = location;

        if (singleLocation != null) {
            singleLocation.success(EventController.createLocationData(location));
            singleLocation = null;

            removeLocationUpdates();
            return;
        }

        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if (serviceIsRunningInForeground(this)) {
            if (!hasNotified) {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification());
                hasNotified = true;
            }
        } else if(hasNotified) {
            hasNotified = false;
        }
    }

    public void changeSettings(String channelName, String title, String content,
                               String iconResName, String iconResPackage, Integer accuracy,
                               long updateInterval, long fastUpdate, Float distance) {
        NOTIFICATION_CHANNEL_NAME = channelName;
        NOTIFICATION_TITLE = title;
        NOTIFICATION_CONTENT = content;
        NOTIFICATION_ICON = getApplicationContext().getResources().getIdentifier(iconResName, iconResPackage, getApplicationContext().getPackageName());
        LOCATION_ACCURACY = accuracy;
        UPDATE_INTERVAL_IN_MILLISECONDS = updateInterval;
        FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = fastUpdate;
        DISTANCE_FILTER = distance;
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }

        return false;
    }
}
