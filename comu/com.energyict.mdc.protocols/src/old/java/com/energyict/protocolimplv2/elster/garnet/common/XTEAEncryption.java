/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.CipheringException;

public class XTEAEncryption {

    private static final int BLOCK_SIZE = 8;
    private static final int DELTA = 0x9E3779B9;
    private static final int XTEA_NR_OF_ROUNDS = 64;

    /**
     * The 128-bits key stored as int[4] *
     */
    private int[] key;

    /**
     * Encrypts a part of a given frame.<br></br>
     * The operation is done in place: there is no return type, but after execution frame will now contain the encrypted data
     *
     * @param frame the byte array
     * @param off   the start index
     * @param len   the number of bytes to encrypt
     */
    public void encrypt(byte[] frame, int off, int len) throws CipheringException {
        int[] data = convertByteArrayToIntArray(ProtocolTools.getSubArray(frame, off, off + len));
        this.encipherData(data);

        // Integrate cipher result back in the original frame
        for (int i = 0; i < len; i++) {
            frame[off + i] = (byte) data[i];
        }
    }

    /**
     * Decrypts a part of a given frame.<br></br>
     * The operation is done in place: there is no return type, but after execution frame will now contain the decrypted data
     *
     * @param bytes the byte array
     * @param off   the start index
     * @param len   the number of bytes to decrypt
     */
    public void decrypt(byte[] bytes, int off, int len) throws CipheringException {
        int[] data = convertByteArrayToIntArray(ProtocolTools.getSubArray(bytes, off, off + len));
        this.decipherData(data);
        for (int i = 0; i < len; i++) {
            bytes[off + i] = (byte) data[i];
        }
    }

    /**
     * Encrypt the frame over the same reception buffer.
     * @param data the frame to encrypt
     */
    private void encipherData(int[] data) throws CipheringException {
        int i, l;
        int[] v = new int[]{0, 0};
        int bufSize = data.length;

        if ((bufSize) % 8 == 0 && (bufSize > 0)) //Test if buffer is multiple of 8 bytes and greater than zero
        {
            for (i = 0, l = (bufSize) / 8; l > 0; l--, i++) {
                v[0] = data[0 + i * 8] << 24 | data[1 + i * 8] << 16 | data[2 + i * 8] << 8 | data[3 + i * 8];
                v[1] = data[4 + i * 8] << 24 | data[5 + i * 8] << 16 | data[6 + i * 8] << 8 | data[7 + i * 8];

                encipher(XTEA_NR_OF_ROUNDS, v, getKey());

                data[0 + i * 8] = (v[0] >>> 24) & 0xFF;
                data[1 + i * 8] = (v[0] >>> 16) & 0xFF;
                data[2 + i * 8] = (v[0] >>> 8) & 0xFF;
                data[3 + i * 8] = (v[0] >>> 0) & 0xFF;

                data[4 + i * 8] = (v[1] >>> 24) & 0xFF;
                data[5 + i * 8] = (v[1] >>> 16) & 0xFF;
                data[6 + i * 8] = (v[1] >>> 8) & 0xFF;
                data[7 + i * 8] = (v[1] >>> 0) & 0xFF;
            }
        } else {
            throw new CipheringException("Failed to encrypt the data stream - Data stream has invalid length");
        }
    }

    /**
     * Decrypt the frame over the same reception buffer.
     * @param data the frame to decrypt
     */
    private void decipherData(int[] data) throws CipheringException {
        int i, l;
        int[] v = new int[]{0, 0};

        if ((data.length) % BLOCK_SIZE == 0 && ((data.length) > 0)) {
            for (i = 0, l = data.length / 8; l > 0; l--, i++) {
                v[0] = data[0 + i * 8] << 24 | data[1 + i * 8] << 16 | data[2 + i * 8] << 8 | data[3 + i * 8];
                v[1] = data[4 + i * 8] << 24 | data[5 + i * 8] << 16 | data[6 + i * 8] << 8 | data[7 + i * 8];
                decipher(XTEA_NR_OF_ROUNDS, v, getKey());
                data[0 + i * 8] = (v[0] >>> 24) & 0xFF;
                data[1 + i * 8] = (v[0] >>> 16) & 0xFF;
                data[2 + i * 8] = (v[0] >>> 8) & 0xFF;
                data[3 + i * 8] = (v[0] >>> 0) & 0xFF;

                data[4 + i * 8] = (v[1] >>> 24) & 0xFF;
                data[5 + i * 8] = (v[1] >>> 16) & 0xFF;
                data[6 + i * 8] = (v[1] >>> 8) & 0xFF;
                data[7 + i * 8] = (v[1] >>> 0) & 0xFF;
            }
        } else {
            throw new CipheringException("Failed to decrypt the data stream - Data stream has invalid length");
        }
    }

    /* take 64 bits of data in v[0] and v[1] and 128 bits of key in k[0] - k[3] */
    private void encipher(int num_rounds, int[] v, int[] key) {
        int v0 = v[0];
        int v1 = v[1];
        int sum = 0;

        for (int i = 0; i < num_rounds; i++) {
            v0 += (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
            sum += DELTA;
            v1 += (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
        }
        v[0] = v0;
        v[1] = v1;
    }

    //decrypt group
    private void decipher(int num_rounds, int[] v, int[] key) {
        int v0 = v[0];
        int v1 = v[1];
        int sum = DELTA * num_rounds;

        for (int i = 0; i < num_rounds; i++) {
            v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
            sum -= DELTA;
            v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
        }
        v[0] = v0;
        v[1] = v1;
    }

    private int[] convertByteArrayToIntArray(byte[] bytes) {
        int[] intArray = new int[bytes.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = bytes[i] & 0xFF;
        }
        return intArray;
    }

    public int[] getKey() {
        return key;
    }

    /**
     * Set the encryption key used for encrypting and decrypting.
     * The key needs to be 16 bytes long.
     *
     * @param key the key
     */
    public void setKey(byte[] key) throws CipheringException {
        if (key.length != 16) {
            throw new CipheringException("Failed to apply the ciphering key - The key must be 8 bytes long");
        }

        int[] keyArray = new int[4];
        for (int i = 0; i < 4; i++) {
            keyArray[i] = ProtocolTools.getIntFromBytes(key, i * 4, 4);
        }
        this.key = keyArray;
    }
}