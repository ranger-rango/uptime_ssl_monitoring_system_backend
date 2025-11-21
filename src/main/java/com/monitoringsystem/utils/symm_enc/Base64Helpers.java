package com.monitoringsystem.utils.symm_enc;

import java.util.Base64;

public class Base64Helpers
{
    public static String toBase64(byte[] data)
    {
        return Base64.getEncoder().encodeToString(data);

    }

    public static byte[] fromBase64(String base64)
    {
        return Base64.getDecoder().decode(base64);
        
    }
}
