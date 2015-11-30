/*
 * Encryptor.java
 *
 * Created on 11 juli 2005, 11:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

/**
 *
 * @author Koen
 */
public class Encryptor {

    /** Creates a new instance of Encryptor */
    private Encryptor() {
    }

    static public byte[] encrypt(byte[] p,byte[] e_key) {
        int i,j;
        int tmp;
        int[] key = new int[4];

        // Swap the password
        byte[] pass = new byte[4];
        pass[1]=p[0];
        pass[0]=p[1];
        pass[3]=p[2];
        pass[2]=p[3];

        byte[] encryptedPass = new byte[4];
        System.arraycopy(pass,0,encryptedPass,0,4);


//        // KV_DEBUG
//        System.out.println("pass");
//        for (int t=0;t<4;t++) {
//            System.out.print(" 0x"+Integer.toHexString((int)p[t]&0xFF));
//        }
//        System.out.println();
//        System.out.println("key");
//        for (int t=0;t<4;t++) {
//            System.out.print(" 0x"+Integer.toHexString((int)e_key[t]&0xFF));
//        }
//        System.out.println();


        // add the arbitrary constant 0xAB41 to a copy of the key
        tmp = ((int)e_key[3]&0xFF)+0x41;
        key[3] = tmp&0xFF;
        tmp = (tmp>>8)+ ((int)e_key[2]&0xFF)+0xAB;
        key[2] = tmp&0xFF;
        tmp = (tmp>>8)+ ((int)e_key[1]&0xFF);
        key[1] = tmp&0xFF;
        tmp = (tmp>>8)+ ((int)e_key[0]&0xFF);
        key[0] = tmp&0xFF;
        tmp=0;

        /* during each step, the key is rotated left through a 33-bit
         * register. Only the low 32 bits are used in each step.
         *
         * At each step, the password is xor'd with the current low 32 bits
         * of the key.
         */

        for (i=(key[3]+key[2]+key[1]+key[0])&0xF;i>=0;i--) {
            for (j=3;j>=0;j--) {
                tmp |= key[j] << 1;
                key[j] = tmp&0xFF;
                encryptedPass[j] = (byte)((int)encryptedPass[j]&0xFF ^ key[j]);
                tmp >>= 8;
            }
        }

        return encryptedPass;
    }

}
