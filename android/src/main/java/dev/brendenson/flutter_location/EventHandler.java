package dev.brendenson.flutter_location;

import android.content.Context;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;

public class EventHandler implements StreamHandler {
    private final String TAG = EventHandler.class.getSimpleName();

    private static final String CHANNEL_ID = "dev.brendenson.flutter_location/onEvent";

    private Context mContext;

    public EventHandler(Context context, BinaryMessenger messenger) {
        this.mContext = context;

        EventChannel eventChannel = new EventChannel(messenger, CHANNEL_ID);
        eventChannel.setStreamHandler(this);
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink event) {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new EventListener(event),
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    public void onCancel(Object o) {

    }
}
