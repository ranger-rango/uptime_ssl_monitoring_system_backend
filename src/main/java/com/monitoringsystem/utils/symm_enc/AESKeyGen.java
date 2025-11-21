package com.monitoringsystem.utils.symm_enc;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AESKeyGen
{
    private static final int AES_KEY_SIZE = 256;

    public static SecretKey keyGen() throws Exception
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }
    
}
