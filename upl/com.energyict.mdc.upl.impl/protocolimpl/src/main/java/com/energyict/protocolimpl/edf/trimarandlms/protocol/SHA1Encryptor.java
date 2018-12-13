/*
 * SHA1Encryptor.java
 *
 * Created on 18 januari 2007, 10:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Koen
 */
public class SHA1Encryptor {
    
    /** Creates a new instance of SHA1Encryptor */
    public SHA1Encryptor() {
    }
    
    static private byte[] getSHA1Hash(byte[] random, byte[] key) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.toString());
        }
        return md.digest(ProtocolUtils.concatByteArrays(random,key));
        
    } // public byte[] getSHA1Hash(byte[] random, byte[] key)

    static public byte[] getCipheredRandomNumber(byte[] random, byte[] key) {
        byte[] hash = getSHA1Hash(random,key);
//System.out.println(ProtocolUtils.outputHexString(hash));        
        byte[] cipheredRandomNumber = new byte[]{hash[0],hash[2],hash[4],hash[6],hash[8],hash[10],hash[12],hash[14]};
        return cipheredRandomNumber;
    }
    
    static public int getMasking16Bit(byte[] random, byte[] key) {
        byte[] hash = getSHA1Hash(random,key);
        int mask = (((int)hash[18]&0xff)<<8) + ((int)hash[19]&0xff);
        return mask;
    }
    
//    public String encrypt(String passWord, String key) {
//        byte[] ciperedPassWord = getCipheredRandomNumber(passWord.getBytes(),key.getBytes());
//        return new String(ciperedPassWord);
//    }
    
    static public void main(String[] args) {
/*
        KV_DEBUG> serverrandom $05$98$0D$8E$42$6A$80$40
KV_DEBUG> ciphered serverrandom $02$D8$3D$D4$17$D0$3F$B8
KV_DEBUG> key $62$69$6C$63$6F$62$63$6C
 *
$17$e0$b0$02 $05 $80$40 $b5$13$05$98$0d$8e$42$6a $80$40 $de$3e$f5$2a$84$61$e7$33 $70$4a 
  */      
        
        byte[] key = new byte[]{(byte)0x62,(byte)0x69,(byte)0x6C,(byte)0x63,(byte)0x6F,(byte)0x62,(byte)0x63,(byte)0x6C};
        //byte[] random = new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xd6,(byte)0x41,(byte)0x87,(byte)0x20};
        //byte[] random = new byte[]{(byte)0x9d,(byte)0x97,(byte)0x7a,(byte)0x15,(byte)0xc7,(byte)0xe4,(byte)0xf3,(byte)0x19};
        //byte[] random = new byte[]{(byte)0xfd,(byte)0x30,(byte)0x99,(byte)0xe6,(byte)0x4a,(byte)0xc3,(byte)0xf6,(byte)0x7d};
        byte[] random = new byte[]{(byte)0x1E,(byte)0xC2,(byte)0x03,(byte)0x1D,(byte)0xBC,(byte)0x81,(byte)0x33,(byte)0x9F};
        
        try {
            String keyStr = new String(key,"UTF-16");
            String clientRandomStr = new String(random,"UTF-16");
        
        System.out.println(ProtocolUtils.outputHexString(random));
        //System.out.println(ProtocolUtils.outputHexString(clientRandomStr.getBytes()));
//        System.out.println(ProtocolUtils.outputHexString(key));
//        System.out.println(ProtocolUtils.outputHexString(keyStr.getBytes()));
        System.out.println();
        byte[] cipheredRandomNumber = SHA1Encryptor.getCipheredRandomNumber(random,key);
        System.out.println(ProtocolUtils.outputHexString(cipheredRandomNumber));
//        System.out.println("0x"+Integer.toHexString(s.getMasking16Bit(clientRandom, key)));
        
        //cipheredRandomNumber = SHA1Encryptor.getCipheredRandomNumber(clientRandomStr.getBytes(),keyStr.getBytes());
        //System.out.println(ProtocolUtils.outputHexString(cipheredRandomNumber));
        
        
        
        
//        byte[] serverRandom = new byte[]{(byte)0x30,(byte)0xc4,(byte)0x2a,(byte)0xcf,(byte)0xfc,(byte)0xd6,(byte)0xbc,(byte)0x4e};
//        cipheredRandomNumber = s.getCipheredRandomNumber(serverRandom,key);
//        System.out.println(ProtocolUtils.outputHexString(cipheredRandomNumber));
//        System.out.println("0x"+Integer.toHexString(s.getMasking16Bit(serverRandom, key)));
        }
        catch(Exception e) {
            
        }
    }
    
    
} // public class SHA1Encryptor
