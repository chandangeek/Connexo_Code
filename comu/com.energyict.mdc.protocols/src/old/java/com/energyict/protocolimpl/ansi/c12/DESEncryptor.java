/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DESEncryptor.java
 *
 * Created on 15 februari 2006, 10:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.protocols.util.ProtocolUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.security.spec.KeySpec;
/**
 *
 * @author Koen
 */
public class DESEncryptor {

    final int DEBUG=0;
    SecretKey secretKey=null;
    Cipher cipher=null;

    /** Creates a new instance of DESEncryptor */
    private DESEncryptor() {
    }

    public static DESEncryptor getInstance(byte[] pass) throws IOException {
        DESEncryptor desEncryptor = new DESEncryptor();
        desEncryptor.init(pass);
        return desEncryptor;
    }

    public void init(byte[] pass) throws IOException {
        try {
            // Create key specification with the password
            KeySpec keySpec = new DESKeySpec(pass);
            // Create key using DES provider
            secretKey = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> Key format: " + secretKey.getFormat());
                System.out.println("KV_DEBUG> Key algorithm: " + secretKey.getAlgorithm());
            }
            cipher = Cipher.getInstance("DES");
            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> Cipher provider: " + cipher.getProvider());
                System.out.println("KV_DEBUG> Cipher algorithm: " + cipher.getAlgorithm());
            }
        } catch(Exception e) {
            throw new NestedIOException(e,"DESEncryptor, init, error "+e.toString());
        }
    }

    public byte[] encrypt(byte[] data) throws IOException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(data);
            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> encrypted data: " + ProtocolUtils.outputHexString(result));
            }
            return result;
        } catch (Exception e) {
            throw new NestedIOException(e,"DESEncryptor, encrypt, error "+e.toString());
        }
    }

    public byte[] decrypt(byte[] data) throws IOException {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] result = cipher.doFinal(data);
            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> decrypted data: " + ProtocolUtils.outputHexString(result));
            }
            return result;
        } catch(Exception e) {
            throw new NestedIOException(e,"DESEncryptor, decrypt, error "+e.toString());
        }

    }

}