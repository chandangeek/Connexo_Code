package com.energyict.encryption.asymetric.util;

import com.energyict.encryption.asymetric.ECCCurve;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 4/02/2016 - 14:44
 */
public class KeyUtilsTest {

    @Test
    public void testSuite1PublicKeyEncoding() throws GeneralSecurityException {
        for (int i = 0; i < 100; i++) {

            ECCCurve suite1Curve = ECCCurve.P256_SHA256;
            PublicKey publicKey = KeyUtils.generateECCKeyPair(suite1Curve).getPublic();

            byte[] encodedPublicKey = KeyUtils.toRawData(suite1Curve, publicKey);

            assertEquals(KeyUtils.toECPublicKey(suite1Curve, encodedPublicKey), publicKey);
        }
    }

    @Test
    public void testSuite2PublicKeyEncoding() throws GeneralSecurityException {
        for (int i = 0; i < 100; i++) {
            ECCCurve suite2Curve = ECCCurve.P384_SHA384;
            PublicKey publicKey = KeyUtils.generateECCKeyPair(suite2Curve).getPublic();

            byte[] encodedPublicKey = KeyUtils.toRawData(suite2Curve, publicKey);

            assertEquals(KeyUtils.toECPublicKey(suite2Curve, encodedPublicKey), publicKey);
        }
    }
}