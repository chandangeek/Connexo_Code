package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.protocol.exception.DataEncryptionException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Decrypts EIWeb messages
 */
public final class Decryptor2 {

    /**
     * The number of bytes that are generated from the seed
     * Currently using MD5 digest that always produces a 128 bit key
     * which is exactly 16 bytes.
     */
    private static final int BYTES_IN_KEY = 16;
    private byte[] md5Seed;
    private byte[] md5Key;
    private int pos;

    Decryptor2(StringBasedMD5Seed md5Seed) {
        this.md5Seed = md5Seed.getBytes();
        pos = 0;
        getMd5Key();
    }


    private void getMd5Key() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw DataEncryptionException.dataEncryptionException(e);
        }
        md5Key = md.digest(md5Seed);
    }

    byte decrypt(byte val) {
        return (byte) (val ^ md5Key[pos++ % BYTES_IN_KEY]);
    }
}