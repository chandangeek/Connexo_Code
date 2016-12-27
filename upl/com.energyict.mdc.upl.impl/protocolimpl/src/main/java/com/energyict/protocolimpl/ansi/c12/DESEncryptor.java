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

import com.energyict.mdc.io.NestedIOException;

import com.energyict.protocol.ProtocolUtils;

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
        } catch(Exception e) {
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
//           DESEncryptor de = DESEncryptor.getInstance((new String("13726687")).getBytes());
           DESEncryptor de = DESEncryptor.getInstance((new String("00000000")).getBytes());
           //byte[] ticket = new byte[]{(byte)0xe1,(byte)0x4c,(byte)0xa5,(byte)0x7e,(byte)0xd9,(byte)0x13,(byte)0x01,(byte)0xdc};

           byte[] ticket = new byte[]{(byte)0x2B,(byte)0xFB,(byte)0xB3,(byte)0x78,(byte)0xFA,(byte)0x5B,(byte)0x2C,(byte)0xA4};
           //ticket: 2B FB B3 78 FA 5B 2C A4

           System.out.println("ticket data: " + ProtocolUtils.outputHexString(ticket));
           byte[] data = de.encrypt(ticket);
           data = ProtocolUtils.getSubArray2(data, 0, 8);
           System.out.println("request data: " + ProtocolUtils.outputHexString(data));
           //authentication: 00 43 72 48 87 D6 4D E6 7E
           data = de.encrypt(data);
           //data = de.decrypt(data);
           data = ProtocolUtils.getSubArray2(data, 0, 8);
           System.out.println("response data: " + ProtocolUtils.outputHexString(data));

// truncated data cannot be decrypted
//           data = de.decrypt(data);
//           System.out.println("1x decrypted response data: " + ProtocolUtils.outputHexString(data));
//           data = de.encrypt(data);
//           System.out.println("2x decrypted response data: " + ProtocolUtils.outputHexString(data));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
