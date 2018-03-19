package com.it_nomads.fluttersecurestorage;

import android.os.Build;
import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher;
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher18Implementation;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterSecureStoragePlugin implements MethodCallHandler {
  private static final String SHARED_PREFERENCES_NAME = "FlutterSecureStorage";

  private static final String KEY_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIHNlY3VyZSBzdG9yYWdlCg";

  private final android.content.SharedPreferences preferences;
  private final android.content.SharedPreferences.Editor editor;
  private final Charset charset;

  private final Map<String, StorageCipher> storageCipherMap = new HashMap<>();

  public static void registerWith(Registrar registrar) {
    FlutterSecureStoragePlugin plugin = new FlutterSecureStoragePlugin(registrar.activity());
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "plugins.it_nomads.com/flutter_secure_storage");
    channel.setMethodCallHandler(plugin);
  }

  private FlutterSecureStoragePlugin(Activity activity) {
    preferences = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    editor = preferences.edit();
    charset = Charset.forName("UTF-8");

    try {
      addStorageCipher(new StorageCipher18Implementation(activity));
    } catch (Exception e) {
      Log.e("no_storage_cipher_18",
            "Could not add StorageCipher18Implementation",
             e);
    }
  }

  private void addStorageCipher(StorageCipher storage) {
    storageCipherMap.put(storage.getStorageName(), storage);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    try {
      Map arguments;
      if (call.arguments instanceof Map) {
        arguments = (Map) call.arguments;
      } else {
        result.error("No arguments", call.method, null);
        return;
      }

      switch (call.method) {
        case "write": {
          String key = (String) arguments.get("key");
          String value = (String) arguments.get("value");
          write(key, value);
          result.success(null);
          break;
        }
        case "read": {
          String key = (String) arguments.get("key");
          String value = read(key);
          result.success(value);
          break;
        }
        case "delete": {
          String key = (String) arguments.get("key");
          delete(key);
          result.success(null);
          break;
        }
        case "readAll": {
          String[] keys = (String[]) arguments.get("keys");
          String[] values = readAll(keys);
          result.success(values);
          break;
        }
        case "writeMap": {
          Map map = (Map) arguments.get("map");
          writeMap(map);
          result.success(null);
          break;
        }
        case "deleteAll": {
          String[] keys = (String[]) arguments.get("keys");
          deleteAll(keys);
          result.success(null);
          break;
        }
        default:
          result.notImplemented();
          break;
      }

    } catch (Exception e) {
      result.error("Exception encountered", call.method, e);
    }
  }

  private void write(String rawKey, String value) throws Exception {
    StorageCipher storageCipher = getStorageCipher();
    if (storageCipher == null) {
      throw new Exception("No storage cipher. Unsupported Android SDK " + Build.VERSION.SDK_INT);
    }

    String key = addPrefixToKey(rawKey);
    byte[] result = storageCipher.encrypt(value.getBytes(charset));
    String storageName = storageCipher.getStorageName();
    String encodedPair = ':' + storageName + ':' + Base64.encodeToString(result, 0);
    editor.putString(key, encodedPair);
    editor.apply();
  }

  private void writeMap(Map<String, String> map) throws Exception {
    for (String rawKey : map.keySet()) {
      String value = map.get(rawKey);

      if (value == null) {
        delete(rawKey);
      } else {
        write(rawKey, value);
      }
    }
  }

  private String read(String rawKey) throws Exception {
    String key = addPrefixToKey(rawKey);
    String encodedPair = preferences.getString(key, null);
    if (encodedPair == null) {
      return null;
    }

    StorageCipher storageCipher;
    String encoded;
    if (encodedPair.startsWith(":")) {
      String[] parts = encodedPair.split(":", 3);
      storageCipher = getStorageCipher(parts[1]);
      encoded = parts[2];
    } else {
      storageCipher = getStorageCipher(StorageCipher18Implementation.CIPHER_STORAGE_NAME);
      encoded = encodedPair;
    }

    if (storageCipher == null) {
      throw new Exception("No storage cipher. Unsupported Android SDK " + Build.VERSION.SDK_INT);
    }

    byte[] data = Base64.decode(encoded, 0);
    byte[] result = storageCipher.decrypt(data);

    return new String(result, charset);
  }
  
  private String[] readAll(String[] rawKeys) throws Exception {
    String[] values = new String[rawKeys.length];

    for (int i = 0; i < rawKeys.length; i++) {
      values[i] = read(rawKeys[i]);
    }

    return values;
  }

  private void delete(String rawKey) throws Exception {
    String key = addPrefixToKey(rawKey);
    editor.remove(key);
    editor.apply();
  }

  private void deleteAll(String[] rawKeys) throws Exception {
    for (String rawKey : rawKeys) {
      delete(rawKey);
    }
  }

  private String addPrefixToKey(String key) {
    return KEY_PREFIX + "_" + key;
  }

  private StorageCipher getStorageCipher() {
    return getStorageCipher(StorageCipher18Implementation.CIPHER_STORAGE_NAME);
  }

  private StorageCipher getStorageCipher(String storageCipherName) {
    return storageCipherMap.get(storageCipherName);
  }
}
