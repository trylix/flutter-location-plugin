#import "FlutterLocationPlugin.h"
#if __has_include(<flutter_location/flutter_location-Swift.h>)
#import <flutter_location/flutter_location-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_location-Swift.h"
#endif

@implementation FlutterLocationPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterLocationPlugin registerWithRegistrar:registrar];
}
@end
