package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.api.crypto.MD5Seed;

import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
* Encrypts data on behalf of unit test classes
* according to what the {@link PacketBuilder} is expecting.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-10-23 (15:23)
*/
public class Encryptor {
    private static final int BYTES_IN_KEY = 16;
    private byte[] md5Seed;
    private byte[] md5Key;
    private int pos;

    public Encryptor (MD5Seed md5Seed) {
        this.md5Seed = md5Seed.getBytes();
        pos = 0;
        this.getMd5Key();
    }


    private void getMd5Key () {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new DataEncryptionException(MessageSeeds.ENCRYPTION_ERROR, e);
        }
        this.md5Key = md.digest(this.md5Seed);
    }

    public byte encrypt (byte val) {
        return (byte) (val ^ this.md5Key[this.pos++ % BYTES_IN_KEY]);
    }

}