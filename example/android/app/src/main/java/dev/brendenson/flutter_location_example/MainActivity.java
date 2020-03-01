package dev.brendenson.flutter_location_example;

import androidx.annotation.NonNull;

import dev.brendenson.flutter_location.ClientHandler;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);
  }

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
}
