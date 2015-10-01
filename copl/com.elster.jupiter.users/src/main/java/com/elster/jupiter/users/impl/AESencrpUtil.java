package com.elster.jupiter.users.impl;

import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.*;

public class AESencrpUtil {

    private static final String ALGORITHM = "AES";

    public String encrypt(String keyVal,String password){
        String encryptedValue= null;
        try {
            Key key = generateKey(keyVal);
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(password.getBytes());
            encryptedValue = new BASE64Encoder().encode(encVal);
        }catch (Exception ex){
            encryptedValue = null;
        }
        return encryptedValue;
    }

    public String decrypt(String keyVal,String encryptedData){
        String decryptedValue= null;
        try {
            Key key = generateKey(keyVal);
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            decryptedValue = new String(decValue);
            return decryptedValue;
        }catch (Exception ex){
            decryptedValue = null;
        }
        return decryptedValue;
    }
    private Key generateKey(String keyVal){
        Key key = null;
        try {
            byte[] keyValue = keyVal.getBytes(Charset.forName("UTF-8"));
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            keyValue = sha.digest(keyValue);
            keyValue = Arrays.copyOf(keyValue, 16);
            key = new SecretKeySpec(keyValue, ALGORITHM);
        }catch (Exception ex){
            key = null;
        }
        return key;
    }

}
