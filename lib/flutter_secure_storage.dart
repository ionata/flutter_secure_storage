import 'dart:async';

import 'package:flutter/services.dart';

/// A key/value secure storage, backed by the platform
class FlutterSecureStorage {
  static const MethodChannel _channel =
      const MethodChannel('plugins.it_nomads.com/flutter_secure_storage');

  /// Writes a value for a given key to the store
  Future<Null> write(String key, String value) {
    if (key == null) throw new ArgumentError.notNull('key');
    if (value == null) throw new ArgumentError.notNull('value');
    return _channel.invokeMethod('write', {
      'key': key,
      'value': value,
    });
  }

  /// Writes a map of key/value pairs to the store
  /// If any of the values is null, the corresponding key will be deleted
  Future<Null> writeMap(Map<String, String> map) {
    if (map == null) throw new ArgumentError.notNull('map');
    if (map.isEmpty) throw new ArgumentError('map must not be empty');
    return _channel.invokeMethod('writeMap', {
      'map': map,
    });
  }

  /// Reads a single value for a given key from the store
  Future<String> read(String key) {
    if (key == null) throw new ArgumentError.notNull('key');
    return _channel.invokeMethod('read', {'key': key});
  }

  /// Reads a list of keys from the store
  Future<List<String>> readAll(List<String> keys) {
    if (keys == null) throw new ArgumentError.notNull('keys');
    if (keys.isEmpty) throw new ArgumentError('keys must not be empty');
    return _channel.invokeMethod('readAll', {'keys': keys});
  }

  /// Deletes a value, associated with a key from the store
  Future<Null> delete(String key) {
    if (key == null) throw new ArgumentError.notNull('key');
    return _channel.invokeMethod('delete', {'key': key});
  }

  /// Deletes all values, associated with a given list of keys from the store
  Future<Null> deleteAll(List<String> keys) {
    if (keys == null) throw new ArgumentError.notNull('keys');
    if (keys.isEmpty) throw new ArgumentError('keys must not be empty');
    return _channel.invokeMethod('deleteAll', {'keys': keys});
  }
}
