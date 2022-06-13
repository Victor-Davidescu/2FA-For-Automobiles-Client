/////////////////////////////////////////////////////////////////////////////////////////////////
//  AUTHOR: VICTOR-FLORIAN DAVIDESCU
//  SID: 1705734
//
//  The code is a modified version from the GitHub project: https://github.com/ijl20/python_java_crypto
////////////////////////////////////////////////////////////////////////////////////////////////
package com.myapp;

import android.annotation.SuppressLint;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.lang.*;
import java.util.Random;

public class Encryption {

    public static String EncryptMessage(String key, String message) {
        try {
            // Generate a random 16-byte initialization vector
            byte[] initVector = new byte[16];
            (new Random()).nextBytes(initVector);
            IvParameterSpec iv = new IvParameterSpec(initVector);

            // Prepare the secret key
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            // Prepare the AES cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

            // Encode the message as array of Bytes
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());

            // Build the output message initVector + encryptedBytes -> base64
            byte[] messageBytes = new byte[initVector.length + encryptedBytes.length];

            System.arraycopy(initVector, 0, messageBytes, 0, 16);
            System.arraycopy(encryptedBytes, 0, messageBytes, 16, encryptedBytes.length);

            @SuppressLint({"NewApi", "LocalSuppress"})
            String encryptedMessage = Base64.getEncoder().encodeToString(messageBytes);

            // Return the encryptedBytes as a Base64-encoded string
            return encryptedMessage;

        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }


    public static String DecryptMessage(String key, String ciphertext) {
        try {
            @SuppressLint({"NewApi", "LocalSuppress"}) byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
            byte[] initVector = Arrays.copyOfRange(encryptedBytes,0,16);
            byte[] messageBytes = Arrays.copyOfRange(encryptedBytes,16,encryptedBytes.length);

            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

            // Convert the ciphertext Base64-encoded String back to bytes, and then decrypt
            byte[] byte_array = cipher.doFinal(messageBytes);

            // Return plaintext as String
            return new String(byte_array, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
