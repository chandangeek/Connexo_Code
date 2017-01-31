/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocols.util.ProtocolUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Koen
 */
public class SHA1Encryptor {

    public SHA1Encryptor() {
    }

    private static byte[] getSHA1Hash(byte[] random, byte[] key) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.toString());
        }
        return md.digest(ProtocolUtils.concatByteArrays(random,key));

    }

    public static byte[] getCipheredRandomNumber(byte[] random, byte[] key) {
        byte[] hash = getSHA1Hash(random,key);
        byte[] cipheredRandomNumber = new byte[]{hash[0],hash[2],hash[4],hash[6],hash[8],hash[10],hash[12],hash[14]};
        return cipheredRandomNumber;
    }

    public static int getMasking16Bit(byte[] random, byte[] key) {
        byte[] hash = getSHA1Hash(random,key);
        int mask = (((int)hash[18]&0xff)<<8) + ((int)hash[19]&0xff);
        return mask;
    }

}