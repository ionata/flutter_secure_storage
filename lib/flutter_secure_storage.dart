import 'dart:async';

import 'package:flutter/services.dart';

class FlutterSecureStorage {
  static const MethodChannel _channel =
      const MethodChannel('plugins.it_nomads.com/flutter_secure_storage');

  Future<Null> write(String key, String value) {
    if (key == null) throw new ArgumentError.notNull('key');
    if (value == null) throw new ArgumentError.notNull('value');
    return _channel.invokeMethod('write', {
      'key': key,
      'value': value,
    });
  }

  Future<Null> writeMap(Map<String, String> map) {
    if (map == null) throw new ArgumentError.notNull('map');
    if (map.isEmpty) throw new ArgumentError('map must not be empty');
    return _channel.invokeMethod('writeMap', {
      'map': map,
    });
  }

  Future<String> read(String key) {
    if (key == null) throw new ArgumentError.notNull('key');
    return _channel.invokeMethod('read', {'key': key});
  }

  Future<List<String>> readAll(List<String> keys) {
    if (keys == null) throw new ArgumentError.notNull('keys');
    if (keys.isEmpty) throw new ArgumentError('keys must not be empty');
    return _channel.invokeMethod('readAll', {'keys': keys});
  }

  Future<Null> delete(String key) {
    if (key == null) throw new ArgumentError.notNull('key');
    return _channel.invokeMethod('delete', {'key': key});
  }

  Future<Null> deleteAll(List<String> keys) {
    if (keys == null) throw new ArgumentError.notNull('keys');
    if (keys.isEmpty) throw new ArgumentError('keys must not be empty');
    return _channel.invokeMethod('deleteAll', {'keys': keys});
  }
}
