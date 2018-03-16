# flutter_secure_storage

A Flutter plugin to store data in secure storage:
* [Keychain](https://developer.apple.com/library/content/documentation/Security/Conceptual/keychainServConcepts/01introduction/introduction.html#//apple_ref/doc/uid/TP30000897-CH203-TP1) is used for iOS 
* [KeyStore](https://developer.android.com/training/articles/keystore.html) is used for Android

*Note* KeyStore was introduced in Android 4.3 (API level 18). The plugin wouldn't work for earlier versions.

## Getting Started
```dart
// Create storage
final storage = new FlutterSecureStorage();

// Write value 
await storage.write('my-key', 'my-value');

// Read value 
String value = await storage.read('my-key');

// Delete value 
await storage.delete('my-key');
```

### Configure Android version 
In `[project]/android/app/build.gradle` set `minSdkVersion` to >= 18.
```
android {
    ...
    
    defaultConfig {
        ...
        minSdkVersion 18
        ...
    }

}
```