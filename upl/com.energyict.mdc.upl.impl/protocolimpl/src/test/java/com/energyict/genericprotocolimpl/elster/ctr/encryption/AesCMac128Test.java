package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import org.junit.Test;

import static com.energyict.protocolimpl.utils.ProtocolTools.concatByteArrays;
import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;
import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 15:58:37
 */
public class AesCMac128Test {

    private static final String HEX_PREFIX = "";
    private static final byte[] KEY = getBytesFromHexString("2B7E151628AED2A6ABF7158809CF4F3C", HEX_PREFIX);

    private static final byte[] T0 = getBytesFromHexString("bb1d6929e95937287fa37d129b756746", HEX_PREFIX);
    private static final byte[] T16 = getBytesFromHexString("070a16b46b4d4144f79bdd9dd04a287c", HEX_PREFIX);
    private static final byte[] T40 = getBytesFromHexString("dfa66747de9ae63030ca32611497c827", HEX_PREFIX);
    private static final byte[] T64 = getBytesFromHexString("51F0BEBF7E3B9D92FC49741779363CFE", HEX_PREFIX);

    private static final byte[] M0 = new byte[0];
    private static final byte[] M16 = concatByteArrays(M0, getBytesFromHexString("6bc1bee22e409f96e93d7e117393172a", HEX_PREFIX));
    private static final byte[] M40 = concatByteArrays(M16, getBytesFromHexString("ae2d8a571e03ac9c9eb76fac45af8e5130c81c46a35ce411", HEX_PREFIX));
    private static final byte[] M64 = concatByteArrays(M40, getBytesFromHexString("e5fbc1191a0a52eff69f2445df4f9b17ad2b417be66c3710", HEX_PREFIX));

    @Test
    public void testEncryptNewObject() throws Exception {
        assertArrayEquals(T0, new AesCMac128(KEY).getAesCMac128(M0));
        assertArrayEquals(T16, new AesCMac128(KEY).getAesCMac128(M16));
        assertArrayEquals(T40, new AesCMac128(KEY).getAesCMac128(M40));
        assertArrayEquals(T64, new AesCMac128(KEY).getAesCMac128(M64));
    }

    @Test
    public void testEncryptionReusedObject() throws Exception {
        AesCMac128 aesCmac128 = new AesCMac128();
        aesCmac128.setKey(KEY);
        assertArrayEquals(T0, aesCmac128.getAesCMac128(M0));
        assertArrayEquals(T16, aesCmac128.getAesCMac128(M16));
        assertArrayEquals(T40, aesCmac128.getAesCMac128(M40));
        assertArrayEquals(T64, aesCmac128.getAesCMac128(M64));
    }

}
