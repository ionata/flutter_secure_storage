package com.it_nomads.fluttersecurestorage.ciphers;

import android.content.SharedPreferences;
import android.util.Base64;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESStorageCipherImplementation implements StorageCipher {
  private static final int ivSize = 16;
  private static final int keySize = 16;
  private static final String KEY_ALGORITHM = "AES";
  private static final String AES_PREFERENCES_KEY = "VGhpcyBpcyB0aGUga2V5IGZvciBhIHNlY3VyZSBzdG9yYWdlIEFFUyBLZXkK";

  private final Key secretKey;
  private final Cipher cipher;
  private final SecureRandom secureRandom;

  public static final String CIPHER_STORAGE_NAME = "AES/CBC/PKCS7Padding";

  @Override
  public String getStorageName() {
    return CIPHER_STORAGE_NAME;
  }

  public AESStorageCipherImplementation(SharedPreferences preferences, StorageCipher18Implementation rsaCipher) throws Exception {
    secureRandom = new SecureRandom();

    String aesKey = preferences.getString(AES_PREFERENCES_KEY, null);
    byte[] key;

    if (aesKey == null) {
      key = new byte[keySize];
      secureRandom.nextBytes(key);
      secretKey = new SecretKeySpec(key, KEY_ALGORITHM);

      byte[] encryptedKey = rsaCipher.wrap(secretKey);

      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(AES_PREFERENCES_KEY, Base64.encodeToString(encryptedKey, Base64.DEFAULT));
      editor.apply();
    } else {
      byte[] encrypted = Base64.decode(aesKey, Base64.DEFAULT);
      secretKey = rsaCipher.unwrap(encrypted, KEY_ALGORITHM);
    }

    cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
  }

  @Override
  public byte[] encrypt(byte[] input) throws Exception {
    byte[] iv = new byte[ivSize];
    secureRandom.nextBytes(iv);

    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

    byte[] payload = cipher.doFinal(input);
    byte[] combined = new byte[iv.length + payload.length];

    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(payload, 0, combined, iv.length, payload.length);

    return combined;
  }

  @Override
  public byte[] decrypt(byte[] input) throws Exception {
    byte[] iv = new byte[ivSize];
    System.arraycopy(input, 0, iv, 0, iv.length);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    int payloadSize = input.length - ivSize;
    byte[] payload = new byte[payloadSize];
    System.arraycopy(input, iv.length, payload, 0, payloadSize);

    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

    return cipher.doFinal(payload);
  }
}
