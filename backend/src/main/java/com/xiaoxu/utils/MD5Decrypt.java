package com.xiaoxu.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MD5Decrypt {

    public static final String SALT = "xiaoxu";

    public String getPassword(String encryptedPassword) {

        byte[] encryptedPasswordBytes = MD5Decrypt.hexStringToByteArray(encryptedPassword);
        String originalPassword = null;
        try {
            originalPassword = MD5Decrypt.decrypt(encryptedPasswordBytes, SALT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Original password: " + originalPassword);
        return originalPassword;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String decrypt(byte[] encryptedPasswordBytes, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update((salt + "").getBytes());
        byte[] originalPasswordBytes = md.digest(encryptedPasswordBytes);
        return Arrays.toString(originalPasswordBytes);
    }
}
