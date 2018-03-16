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

  Future<String> read(String key) {
    if (key == null) throw new ArgumentError.notNull('key');
    return _channel.invokeMethod('read', {'key': key});
  }

  Future<Null> delete(String key) {
    if (key == null) throw new ArgumentError.notNull('key');
    return _channel.invokeMethod('delete', {'key': key});
  }
}
