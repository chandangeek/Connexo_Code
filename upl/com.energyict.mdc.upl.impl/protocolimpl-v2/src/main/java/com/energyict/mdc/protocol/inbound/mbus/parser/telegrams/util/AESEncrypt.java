package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class AESEncrypt {

    private static final String ALGO = "AES";
    private static final String ALGO_MODE = "/CBC/NoPadding";

    public static byte[] decrypt(byte[] encryptedData, byte[] keyArr, byte[] ctrInitVector) throws Exception {
        Key key = generateKey(keyArr);
        Cipher c = Cipher.getInstance(ALGO + ALGO_MODE);
        c.init(Cipher.DECRYPT_MODE, key, genIvParameterSpec(ctrInitVector));
        byte[] decValue = c.doFinal(encryptedData);
        return decValue;
    }

    private static Key generateKey(byte[] keyArr) throws Exception {
        Key key = new SecretKeySpec(keyArr, ALGO);
        return key;
    }

    private static IvParameterSpec genIvParameterSpec(byte[] vector) {
        IvParameterSpec ivSpec = new IvParameterSpec(vector);
        return ivSpec;
    }

}