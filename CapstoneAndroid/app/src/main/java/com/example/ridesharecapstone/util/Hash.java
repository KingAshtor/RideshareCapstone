package com.example.ridesharecapstone.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private static MessageDigest SHA_256;
    static {
        try {
            SHA_256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String doubleHash(String input, String salt) {
        final String simpleHash = toHexString(SHA_256.digest(input.getBytes(StandardCharsets.UTF_8)));
        return toHexString(SHA_256.digest(salt.concat(simpleHash).getBytes(StandardCharsets.UTF_8)));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}
