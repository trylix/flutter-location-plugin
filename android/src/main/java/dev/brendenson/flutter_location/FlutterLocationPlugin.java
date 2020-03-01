package dev.brendenson.flutter_location;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import io.flutter.Log;
import io.flutter.app.FlutterActivity;
import io.flutter.app.FlutterActivityEvents;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.service.ServiceAware;
import io.flutter.embedding.engine.plugins.service.ServicePluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterLocationPlugin
 */
public class FlutterLocationPlugin implements FlutterPlugin, ActivityAware {
    private static final String CHANNEL_ID = "dev.brendenson.flutter_location/onRequest";

    private static ClientHandler handler;
    private static BinaryMessenger messenger;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        messenger = flutterPluginBinding.getBinaryMessenger();
    }

    public static void registerWith(Registrar registrar) {
        messenger = registrar.messenger();

        registerServicePlugin(registrar.activity());
    }

    private static void registerServicePlugin(Context context) {
        handler = new ClientHandler(messenger);

        handler.registerActivity(context);

        MethodChannel methodChannel = new MethodChannel(messenger, CHANNEL_ID);
        methodChannel.setMethodCallHandler(handler);

        handler.onStartListening();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (handler != null) {
            handler.onStopListening();
        }

        handler = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        registerServicePlugin(binding.getActivity());
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        if (handler != null) {
            handler.onStopListening();
        }

        handler = null;
    }
}
