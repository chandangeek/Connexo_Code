/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license.impl;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class LicenseVerifier {

    private static final byte[] bytes = {
            48, -126, 1, -74, 48, -126, 1, 43, 6, 7, 42, -122, 72, -50, 56, 4, 1, 48, -126, 1, 30, 2, -127, -127, 0,
            -51, 100, -119, -10, 62, -124, -45, 87, -28, -87, -110, -93, 123, 121, -9, -72, 88, 127, 105, 12, 27, 125,
            13, 62, 1, 10, -128, -68, -25, -55, 127, 4, -60, -106, -9, -109, -61, -45, -98, -76, -69, 63, -30, -64, -91,
            31, 87, -80, -40, -55, 56, -72, 121, -109, 64, 38, 92, -48, -9, -21, 72, 16, 70, 11, -92, -70, -87, 97, -6,
            -16, 63, 95, -51, 71, -13, 68, -38, -127, -100, 108, 124, 42, -69, 114, -59, 94, -84, 91, 125, 56, -35,
            -113, -49, -81, -43, 10, -46, -56, 2, 37, -37, -69, 91, -97, -36, 122, 10, -127, 30, -70, -103, -115, 23,
            119, -103, 110, -112, -46, 127, 12, 5, 4, -32, -22, 19, 6, -51, 83, 2, 21, 0, -114, 3, -14, 76, -123, 103,
            -112, 7, 19, -65, -64, -84, 61, 62, -4, 127, -91, 105, -23, -61, 2, -127, -128, 74, 21, -5, -67, -92, 65, 6,
            -122, 108, 6, -67, 51, 46, -98, 68, -80, -8, -113, 115, 54, -21, 14, 118, 24, -8, 13, 49, 52, -8, -78, 41,
            -17, -67, 125, 73, -98, -100, 72, -24, -5, 13, 104, 83, -128, -74, 52, -105, -95, -30, 106, 53, 69, 57, -41,
            -82, -125, -40, -49, 113, 80, 15, 18, 9, -18, 49, 8, 113, 47, -106, -23, -36, -72, -33, -51, 5, -25, -47,
            -30, -10, 61, 108, -16, 119, -65, -121, -34, 45, -64, 52, -42, -53, -55, 82, 107, -69, -104, -65, 86, -80,
            -47, -102, 90, 126, -126, -48, 14, -38, -47, 48, -75, -65, -84, -44, -124, -47, 119, 89, 124, 126, -75, -23,
            -34, 49, 70, 81, -71, 46, -72, 3, -127, -124, 0, 2, -127, -128, 118, 117, -119, -124, 10, -125, -24, 90,
            -116, 21, 78, 92, -68, -25, 40, -121, -38, -47, 84, 101, -126, -101, 87, 73, 76, -15, -109, 51, 80, 6, 123,
            76, -46, -48, 82, -44, -78, 9, 7, 6, 113, -91, -109, -78, -85, -25, 59, 19, 95, -56, 68, -30, 52, -106, -27,
            108, -8, 72, 95, -108, -26, -2, -112, 24, -97, 52, -108, -58, -55, 117, 16, 2, -75, -1, 111, 60, -63, -74,
            21, -55, 49, 8, 10, 110, 52, -37, -67, -105, 37, -37, 89, 0, -91, -63, 92, -61, 78, -42, -87, 98, -101, 55,
            108, -4, -3, 71, 50, 36, 5, -104, -75, -68, -71, -48, 15, -10, -95, 45, 93, 16, -120, -118, -80, -6, -95,
            114, -102, 29
    };

    private PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public Object extract(SignedObject signedObject) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IOException, SignatureException, ClassNotFoundException {
        Signature verificationEngine = Signature.getInstance("SHA1withDSA");
        if (signedObject.verify(getKey(), verificationEngine)) {
            return signedObject.getObject();
        } else {
            return null;
        }
    }
}
