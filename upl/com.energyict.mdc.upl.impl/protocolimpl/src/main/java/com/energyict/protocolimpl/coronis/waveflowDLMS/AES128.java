package com.energyict.protocolimpl.coronis.waveflowDLMS;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class AES128 {

    private byte[] encryptionKey;
    private Cipher aesCBCCipher;

    public AES128(byte[] encryptionKey) {
        setKey(encryptionKey);
    }

    public final void setKey(byte[] key) {
        this.encryptionKey = key;
    }

    public byte[] decrypt(byte[] encryptedData) {
        SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey, "AES");

        // Instantiate the cipher
		try {

	        // build the initialization vector.  This example is all zeros, but it 
	        // could be any value or generated using a random number generator.
	        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	        IvParameterSpec ivspec = new IvParameterSpec(iv);
	        // initialize the cipher for encrypt mode
	        getAesCBCCipher().init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
			
	        byte[] data = getAesCBCCipher().doFinal(encryptedData);
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
		try {
			
	        // build the initialization vector.  This example is all zeros, but it 
	        // could be any value or generated using a random number generator.
	        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	        IvParameterSpec ivspec = new IvParameterSpec(iv);
	        // initialize the cipher for encrypt mode
			getAesCBCCipher().init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
			
//			cipher = Cipher.getInstance("AES");
//	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			
	        byte[] encrypted = getAesCBCCipher().doFinal(data);
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

    private Cipher getAesCBCCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
    	if (aesCBCCipher == null) {
			aesCBCCipher = Cipher.getInstance("AES/CBC/NoPadding");
		}
    	return aesCBCCipher;
	}
}