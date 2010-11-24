package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

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
			cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
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
		}

        return null;
    	
    }
    
    public byte[] encrypt(byte[] data) {

        SecretKeySpec skeySpec = new SecretKeySpec(encryptionKey, "AES");

        // Instantiate the cipher
        Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
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
		}
        return null;
    }

}