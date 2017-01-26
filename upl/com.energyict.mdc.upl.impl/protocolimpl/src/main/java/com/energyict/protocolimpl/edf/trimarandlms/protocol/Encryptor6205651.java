/*
 * Encryptor6205651.java
 *
 * Created on 18 januari 2007, 11:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class Encryptor6205651 {
    
    
    private final int POLY_CRYPTAGE=0xD008; /* 4 bits - 1 ==> 16 sommes possibles */
    
    private final int ENTREE = 0;
    private final int SORTIE = 1;
    //cryptage[0..15][0=entree, 1=sortie]
    private final int[][] cryptage= new int[][]{
        /* entree - sortie */
        { 0x0000, 0x0000 },
        { 0x0008, 0x0001 },
        { 0x1000, 0x0001 },
        { 0x1008, 0x0000 },
        { 0x4000, 0x0001 },
        { 0x4008, 0x0000 },
        { 0x5000, 0x0000 },
        { 0x5008, 0x0001 },
        { 0x8000, 0x0001 },
        { 0x8008, 0x0000 },
        { 0x9000, 0x0000 },
        { 0x9008, 0x0001 },
        { 0xC000, 0x0000 },
        { 0xC008, 0x0001 },
        { 0xD000, 0x0001 },
        { 0xD008, 0x0000 }
    };
    
    
    
    
    
    /**
     * Creates a new instance of Encryptor6205651 
     */
    public Encryptor6205651() {
    }
    
    
    public byte[] getEncryptedData(byte[] data,int key) {
        byte[] encryptedData = new byte[data.length];
        // Tantque (il reste au moins deux octets e crypter) Faire Former une sequence de 16 bits e crypter Crypter cette sequence (XOR avec la cle = somme bit e bit) Ranger la sequence cryptee e la place de la sequence source
        int pointer=0,encryptedDataPointer=0;
        int i;
        int tmp;
        for (i = data.length; i > 1; i -= 2) {
//System.out.println(Integer.toHexString(key));
            tmp = ((int)data[pointer++]&0xff) << 8;
            tmp = (tmp ^ key) & 0xFF00;
            encryptedData[encryptedDataPointer++] = (byte)(tmp >> 8);
            tmp |= ((int)data[pointer++]&0xff);
            tmp = tmp ^ (key & 0x00FF);
            encryptedData[encryptedDataPointer++] = (byte)(tmp & 0xFF);
            
            // Mettre e jour la cle de cryptage F tantque
            tmp = key << 1 &0xffff; /* a'(i) = a(i+1) */
            key = key & POLY_CRYPTAGE; /* AB = a1b1 a2b2 a3b3 ... anbn */
            int j = 0;
            while (key != cryptage[j][ENTREE]) {
                j += 1;
            }
            key = (tmp | cryptage[j][SORTIE]) & 0xffff;

        }
        
        // Si (il reste un octet e crypter) Alors Former la sequence de 16 bits e crypter Crypter cette sequence Ranger la sequence de 8 bits cryptee Fsi
        
        if (i == 1) {
            tmp = ((int)data[pointer++]&0xff) << 8;
            encryptedData[encryptedDataPointer++] = (byte)((tmp ^ key) >> 8);
        }
        return encryptedData;
        
    } // public byte[] getEncryptedData(byte[] data,int key)
    
    static public void main(String[] args) {
        
        SHA1Encryptor s = new SHA1Encryptor();
        byte[] passwordKey = new byte[]{(byte)0x62,(byte)0x69,(byte)0x6C,(byte)0x63,(byte)0x6F,(byte)0x62,(byte)0x63,(byte)0x6C};
        //byte[] serverRandom = new byte[]{(byte)0x30,(byte)0xc4,(byte)0x2a,(byte)0xcf,(byte)0xfc,(byte)0xd6,(byte)0xbc,(byte)0x4e};
        byte[] serverRandom = new byte[]{(byte)0x9d,(byte)0x97,(byte)0x7a,(byte)0x15,(byte)0xc7,(byte)0xe4,(byte)0xf3,(byte)0x19};
        int masking = SHA1Encryptor.getMasking16Bit(serverRandom, passwordKey);
        System.out.println("0x"+Integer.toHexString(masking));
        
        Encryptor6205651 e = new Encryptor6205651();
        //byte[] data = new byte[]{(byte)0x4e,(byte)0xf9,(byte)0x93,(byte)0xf0,(byte)0x2b,(byte)0xe1,(byte)0x45,(byte)0xc2,(byte)0x80,(byte)0x7e,(byte)0x1b,(byte)0x2b,(byte)0x86,(byte)0x26,(byte)0x7c,(byte)0x03,(byte)0xad,(byte)0xbb,(byte)0x9b,(byte)0xA4,(byte)0x6B};
        //byte[] data = new byte[]{(byte)0x4d,(byte)0xf9,(byte)0x93,(byte)0xf0,(byte)0x2b};
        //byte[] data = new byte[]{(byte)0x0c,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x0f,(byte)0x00,(byte)0x04,(byte)0x28,(byte)0xb8,(byte)0x21,(byte)0x00,(byte)0x0c,(byte)0x55};
        //byte[] data = new byte[]{(byte)0x4d,(byte)0xf9,(byte)0x93,(byte)0xf1,(byte)0xbb};
        byte[] data = new byte[]{(byte)0x4d,(byte)0xf9,(byte)0x93,(byte)0xf1,(byte)0xa3};
        
        System.out.println(ProtocolUtils.outputHexString(data));
        
        byte[] encryptedData = e.getEncryptedData(data,masking); 
        System.out.println(ProtocolUtils.outputHexString(encryptedData));
        
        encryptedData = e.getEncryptedData(encryptedData,masking);
        System.out.println(ProtocolUtils.outputHexString(encryptedData));        
    }
    
} // public class Encryptor6205651
