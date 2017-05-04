/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.ansi.c12.EAXPrime;

import com.energyict.encryption.AesGcm;
import com.energyict.encryption.BitVector;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 *An implementation of the EAX' (EAX PRIME) Cryptographic Mode
 * For further details see:     + NIST standard - EAX' Cipher Mode (May 2011)
 *                              + ANSI standard - C12.22-2008 Protocol specification For Interfacing to Data Communication Networks standard - Annex I.
 *
 *  Black box representation of EAX' Cryptography
 *          ---------------------------------------------------
 *          |       N           |             P               |
 *          ---------------------------------------------------
 *                           ------      |
 *                  K --->  | EAX' |     |
 *                           ------      v
 *
 *          ---------------------------------------------------------
 *          |      N           |             C                | T   |
 *          ---------------------------------------------------------
 *
 *  In this, the components are defined as:
 *  + ClearText (N)     Part of the message that is authenticated but not encrypted for secrecy
 *  + PlainText (P)     Part of the message which is encrypted for secrecy
 *  + Key (K)           The cryptographic secret key
 *
 *  + CipherText (C)    part of the message after encryption for secrecy, corresponding directly to the plain text P
 *  + MAC (T)           4 bytes Message Authentication Code
 *
 *  For usage examples, please see test class EAXPrimeEncoderTest *
 *
 * This implementation is based the EAX' implementation, as documented in the NIST standard "EAX' Cipher Mode (May 2011)".
 * Note: Under the hood, this algorithm uses an AES-128 encryption algorithm - the one used here is the AES Galois/Counter mode of Operation(GCM)(AesGcm128).
 *
 * WARNING: the implementation provided in ANSI standard 'C12.22-2008 Protocol specification For Interfacing to Data Communication Networks standard' - Annex I is slightly different from the NIST one!
 * >> DO NOT USE IT!
 *
 */
public class EAXPrimeEncoder {

    static byte[] buffer = new byte[16];
    static int buffer_idx;
    static byte[] d_value = new byte[16];
    static byte[] q_value = new byte[16];
    static byte[] nonce = new byte[16];

    private byte[] plainText;
    private byte[] cipherText;
    private byte[] mac = new byte[4];

    private byte[] encryptionKey;
    private AesGcm aesEngine;

    public EAXPrimeEncoder(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
        init();
    }

    /**
     * Initialize the encryption - based on the encryption key.
     * This needs to be done once, then it can be reused.
     */
    private void init() {
        // A. Initialize the AES encryption engine
        aesEngine = new AesGcm(new BitVector(encryptionKey));

        // B. Derive key dependent constants
        d_value = new byte[16];
        byte[] l_value = aesEncryptData(d_value);

        d_value = Dbl(l_value);
        q_value = Dbl(d_value);
    }

    /**
     * ********************************************************************************************
     * Encrypt the given clearText and PlainText
     * *********************************************************************************************
     */
    public void encrypt(byte[] clearText, byte[] plainText) {
        byte[] tagN;
        byte[] tagC;
        byte[] T = new byte[16];
        this.plainText = plainText;

        // CMAC'(D, ClearText)
        CMacStart('D');
        CMacNext(clearText);
        tagN = CMacEnd();

        /* Encryption of the payload, using TagN as nonce */
        if (plainText != null) {
            cipherText = AesCtr(tagN, plainText);
        } else {
            mac = ProtocolTools.getSubArray(tagN, 12, 16);   // Mac bytes are the last 4 bytes of tagN
            cipherText = new byte[16];
            return;
        }

        // CMAC'(Q, CipherText)
        CMacStart('Q');
        CMacNext(cipherText);
        tagC = CMacEnd();

        // T = TagN XOR TagC
        for (int i = 0; i < 16; i++) {
            T[i] = (byte) (((int) tagN[i] & 0xFF) ^ ((int) tagC[i] & 0xFF));
        }

        mac = ProtocolTools.getSubArray(T, 12, 16);   // Mac bytes are the last 4 bytes of T
    }

    /**
     * ********************************************************************************************
     * Decrypt the given clearText and CipherText
     * *********************************************************************************************
     */
    public byte[] decrypt(byte[] clearText, byte[] cipherText, byte[] expectedMac) throws IOException {
        byte[] tagN;
        byte[] tagC;
        byte[] T = new byte[16];
        this.cipherText = cipherText;

        // CMAC'(D, ClearText)
        CMacStart('D');
        CMacNext(clearText);
        tagN = CMacEnd();

        /* Decryption of the payload, using TagN as nonce */
        if (cipherText != null) {
            // CMAC'(Q, CipherText)
            CMacStart('Q');
            CMacNext(cipherText);
            tagC = CMacEnd();

            // T = TagN XOR TagC
            for (int i = 0; i < 16; i++) {
                T[i] = (byte) (((int) tagN[i] & 0xFF) ^ ((int) tagC[i] & 0xFF));
            }

            this.mac = ProtocolTools.getSubArray(T, 12, 16);     // Mac bytes are the last 4 bytes of T
        } else {
            this.mac = ProtocolTools.getSubArray(tagN, 12, 16);   // Mac bytes are the last 4 bytes of tagN
        }

        if (!ProtocolTools.getHexStringFromBytes(this.mac, "").equalsIgnoreCase(ProtocolTools.getHexStringFromBytes(expectedMac, ""))) {
            throw new IOException("EAXPrimeEncoder - Failed to decrypt the frame: Received MAC doesn't match the calculated one.");
        }

        if (cipherText != null) {
            plainText = AesCtr(tagN, cipherText);
        } else {
            plainText = null;
        }
        return plainText;
    }

    /**
     * ********************************************************************************************
     * Description: Initiate a CMAC computation
     * <p/>
     * Inputs: key = 16 bytes key
     * init_with = can be set to either 'D' or 'Q'
     * *********************************************************************************************
     */
    private void CMacStart(char init_with) {
        buffer_idx = 0;
        if (init_with == 'D') {
            nonce = d_value.clone();
        } else {
            nonce = q_value.clone();
        }
    }

    /**
     * ********************************************************************************************
     * Description: Add an array of bytes to the CMAC computation
     * <p/>
     * Inputs: byte = Array of bytes to be included in the CMAC computation
     * *********************************************************************************************
     */
    private void CMacNext(byte[] s) {
        int i, j;

        for (i = 0; i < s.length; i++) {
            if (buffer_idx == 16) {

                for (j = 0; j < 16; j++) {
                    nonce[j] ^= ((int) buffer[j] & 0xFF);
                }

                nonce = aesEncryptData(nonce);
                buffer_idx = 0;
            }

            buffer[buffer_idx++] = (byte) ((int) s[i] & 0xFF);
        }
    }

    /**
     * ********************************************************************************************
     * Description: Complete the CMAC computation
     * *********************************************************************************************
     */
    private byte[] CMacEnd() {
        int i;

        if (buffer_idx == 16) {
            for (i = 0; i < 16; i++) {
                buffer[i] ^= d_value[i];
            }
        } else {
            buffer[buffer_idx++] = (byte) 0x80;
            for (i = buffer_idx; i < 16; i++) {
                buffer[i] = 0;
            }
            for (i = 0; i < 16; i++) {
                buffer[i] ^= q_value[i];
            }
        }

        for (i = 0; i < 16; i++) {
            nonce[i] ^= buffer[i];
        }

        nonce = aesEncryptData(nonce);

        return nonce;
    }

    /**
     * ********************************************************************************************
     * Description: Functions that encrypts or decrypt an array of bytes.
     *
     * Inputs:  nonce = Nonce value used during the execution of this function
     *          plainText = The text that needs to be encrypted OR the cipherText that needs to be decrypted
     * *********************************************************************************************
     */
    private byte[] AesCtr(byte[] nonce, byte[] plainText) {

        int m = (int) Math.ceil((float) plainText.length / 16);
        byte[] myNonce = nonce.clone();
        myNonce[12] &= 0x7F;
        myNonce[14] &= 0x7F;

        String paddByteString = new String();

        for (int i = 0; i < m; i++) {
            byte[] encryptedMyNonce = aesEncryptData(myNonce);
            paddByteString += ProtocolTools.getHexStringFromBytes(encryptedMyNonce, "");

            /* Incrementing the nonce */
            myNonce[15]++;
            if (myNonce[15] == 0) {
                myNonce[14]++;
                if (myNonce[14] == 0) {
                    myNonce[13]++;
                    if (myNonce[13] == 0) {
                        myNonce[12]++;
                    }
                }
            }
        }

        byte[] paddBytes = ProtocolTools.getBytesFromHexString(paddByteString, "");
        byte[] result = new byte[plainText.length];
        for (int i = 0; i < plainText.length; i++) {
            result[i] = (byte) (plainText[i] ^ paddBytes[i]);
        }

        return result;
    }

    private byte[] Dbl(byte[] in) {
        int i;
        byte[] out = in.clone();
        boolean carry = (out[15] & 0x80) != 0;

        for (i = 15; i > 0; i--) {
            out[i] <<= 1;
            if ((out[i - 1] & 0x80) != 0) {
                out[i] |= 0x01;
            }
        }
        out[0] <<= 1;

        if (carry) {
            out[0] ^= 0x87;
        }
        return out;
    }

    private AesGcm getAESEngine() {
        return aesEngine;
    }

    private byte[] aesEncryptData(byte[] in) {
        BitVector out = getAESEngine().aesEncrypt(new BitVector(in));
        return out.getValue();
    }
    public byte[] getCipherText() {
        return cipherText;
    }

    public byte[] getMac() {
        return mac;
    }

    public byte[] getPlainText() {
        return plainText;
    }
}