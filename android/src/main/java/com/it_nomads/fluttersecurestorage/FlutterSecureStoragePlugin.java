package com.it_nomads.fluttersecurestorage;

import android.os.Build;
import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher;
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher18Implementation;
import com.it_nomads.fluttersecurestorage.ciphers.AESStorageCipherImplementation;

import java.nio.charset.Charset;
import java.util.ArrayList;
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
    editor.apply();
    charset = Charset.forName("UTF-8");

    StorageCipher18Implementation rsaCipher = null;
    try {
      rsaCipher = new StorageCipher18Implementation(activity);
      addStorageCipher(rsaCipher);
    } catch (Exception e) {
      Log.e("no_storage_cipher_18",
            "Could not add StorageCipher18Implementation",
             e);
    }

    try {
      if (rsaCipher == null) throw new Exception("No RSA Cipher");
      addStorageCipher(new AESStorageCipherImplementation(preferences, rsaCipher));
    } catch (Exception e) {
      Log.e("no_aes_storage_cipher",
              "Could not add AESStorageCipherImplementation",
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
        if (arguments.size() == 0) {
          result.error("No arguments", call.method, null);
          return;
        }
      } else {
        result.error("No arguments", call.method, null);
        return;
      }

      switch (call.method) {
        case "write": {
          String key = call.argument("key");
          String value = call.argument("value");
          write(key, value);
          result.success(null);
          break;
        }
        case "read": {
          String key = call.argument("key");
          String value = read(key);
          result.success(value);
          break;
        }
        case "delete": {
          String key = call.argument("key");
          delete(key);
          result.success(null);
          break;
        }
        case "readAll": {
          ArrayList<String> keys = call.argument("keys");
          ArrayList<String> values = readAll(keys);
          result.success(values);
          break;
        }
        case "writeMap": {
          Map<String, String> map = call.argument("map");
          writeMap(map);
          result.success(null);
          break;
        }
        case "deleteAll": {
          ArrayList<String> keys = call.argument("keys");
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
    byte[] result;

    try {
      result = storageCipher.encrypt(value.getBytes(charset));
    } catch (Exception e) {
      Log.e("unable_to_encrypt",
              "Could not encrypt with storage: " + storageCipher.getStorageName(),
              e);
      throw e;
    }

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
    try {
      byte[] result = storageCipher.decrypt(data);
      return new String(result, charset);
    } catch (Exception e) {
      Log.e("unable_to_decrypt",
              "Could not decrypt with storage: " + storageCipher.getStorageName(),
              e);
      throw e;
    }
  }
  
  private ArrayList<String> readAll(ArrayList<String> rawKeys) throws Exception {
    ArrayList<String> values = new ArrayList<>();

    for (String rawKey : rawKeys) {
      values.add(read(rawKey));
    }

    return values;
  }

  private void delete(String rawKey) {
    String key = addPrefixToKey(rawKey);
    editor.remove(key);
    editor.apply();
  }

  private void deleteAll(ArrayList<String> rawKeys) {
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
