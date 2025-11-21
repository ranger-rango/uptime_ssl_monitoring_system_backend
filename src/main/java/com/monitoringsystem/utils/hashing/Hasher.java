package com.monitoringsystem.utils.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher
{
    public static String hashSHA512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
