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
            tmp = (int)((tmp ^ key) & 0xFF00);
            encryptedData[encryptedDataPointer++] = (byte)(tmp >> 8);
            tmp |= ((int)data[pointer++]&0xff);
            tmp = (int)(tmp ^ (key & 0x00FF));
            encryptedData[encryptedDataPointer++] = (byte)(tmp & 0xFF);

            // Mettre e jour la cle de cryptage F tantque
            tmp = (int)(key << 1)&0xffff; /* a'(i) = a(i+1) */
            key = (int)(key & POLY_CRYPTAGE); /* AB = a1b1 a2b2 a3b3 ... anbn */
            int j = 0;
            while (key != cryptage[j][ENTREE]) {
                j += 1;
            }
            key = (int)(tmp | cryptage[j][SORTIE]) & 0xffff;

        }

        // Si (il reste un octet e crypter) Alors Former la sequence de 16 bits e crypter Crypter cette sequence Ranger la sequence de 8 bits cryptee Fsi

        if (i == 1) {
            tmp = ((int)data[pointer++]&0xff) << 8;
            encryptedData[encryptedDataPointer++] = (byte)((tmp ^ key) >> 8);
        }
        return encryptedData;

    }

}