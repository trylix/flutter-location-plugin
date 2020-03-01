package dev.brendenson.flutter_location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import io.flutter.plugin.common.EventChannel;

public class EventListener extends BroadcastReceiver {
    private EventChannel.EventSink mEvent;

    public EventListener(EventChannel.EventSink event) {
        this.mEvent = event;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);

        if (location != null) {
            mEvent.success(EventController.createLocationData(location));
        } else {
            mEvent.error("Location is not available","","");
        }
    }
}
