# flutter_secure_storage

A Flutter plugin to store data in secure storage:

* [Keychain](https://developer.apple.com/library/content/documentation/Security/Conceptual/keychainServConcepts/01introduction/introduction.html#//apple_ref/doc/uid/TP30000897-CH203-TP1) is used for iOS 
* [KeyStore](https://developer.android.com/training/articles/keystore.html) is used for Android

*Note* KeyStore was introduced in Android 4.3 (API level 18). The plugin wouldn't work for earlier versions.

## Breaking changes

* Android
  - [5fefab][5fefab] This fork, internally, stores the used cipher in the value, in the format ':cipher:data'

## Getting Started

```dart
// Create storage
final storage = new FlutterSecureStorage();

// Write value 
await storage.write('my-key', 'my-value');

// Read value 
String value = await storage.read('my-key');
print(value); // my-value

// Delete value 
await storage.delete('my-key');
print(await storage.read('my-key')); // null
```

On Android all values are stored encrypted in shared preferences.

**NOTE:** Writing a `null` is not accepted. Use `delete`.

## Working with multiple keys

```dart
// Write a Map<String, String>
await storage.writeMap({
  'username': 'me@flutter.io',
  'password': 'secret',
});

// NOTE: writeMap will delete any key, whose value is `null`
await storage.writeMap({
  'username': 'me+again@flutter.io',
  'password': null,
});
print(await storage.read('password') == null); // true

// Read multiple keys
List<String> values = await storage.readAll([
  'username',
  'password',
]);
print(values); // ['me+again@flutter.io', null]

// Delete multiple keys
await storage.deleteAll(['username', 'password']);
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

[5fefab]: https://github.com/ionata/flutter_secure_storage/commit/5fefabfd1cf49f2dcae547a130f8b970ee4bec0d