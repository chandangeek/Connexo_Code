/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES128 {

    private byte[] encryptionKey;

    public AES128(byte[] encryptionKey) {
        setKey(encryptionKey);
    }

    public final void setKey(byte[] key) {
        this.encryptionKey = key;
    }

    public byte[] decrypt(byte[] encryptedData) {
        SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey, "AES");

        // Instantiate the cipher
        Cipher cipher;
		try {

	        // build the initialization vector.  This example is all zeros, but it
	        // could be any value or generated using a random number generator.
	        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	        IvParameterSpec ivspec = new IvParameterSpec(iv);
	        // initialize the cipher for encrypt mode
	        cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

	        byte[] data = cipher.doFinal(encryptedData);
	        return data;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return null;

    }

    public byte[] encrypt(byte[] data) {

        SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey, "AES");

        // Instantiate the cipher
        Cipher cipher;
		try {

	        // build the initialization vector.  This example is all zeros, but it
	        // could be any value or generated using a random number generator.
	        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	        IvParameterSpec ivspec = new IvParameterSpec(iv);
	        // initialize the cipher for encrypt mode
	        cipher = Cipher.getInstance("AES/CBC/NoPadding"); //PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);

//			cipher = Cipher.getInstance("AES");
//	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

	        byte[] encrypted = cipher.doFinal(data);
	        return encrypted;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
        return null;
    }

}