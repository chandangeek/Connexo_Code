/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.util.Arrays;
import java.util.Optional;

/**
 * Known KeyUsages
 * Inspired by org.bouncycastle.asn1.x509.KeyUsage
 */
public enum KeyUsage {
    digitalSignature(0, org.bouncycastle.asn1.x509.KeyUsage.digitalSignature),
    nonRepudiation(1, org.bouncycastle.asn1.x509.KeyUsage.nonRepudiation),
    keyEncipherment(2, org.bouncycastle.asn1.x509.KeyUsage.keyEncipherment),
    dataEncipherment(3, org.bouncycastle.asn1.x509.KeyUsage.dataEncipherment),
    keyAgreement(4, org.bouncycastle.asn1.x509.KeyUsage.keyAgreement),
    keyCertSign(5, org.bouncycastle.asn1.x509.KeyUsage.keyCertSign),
    cRLSign(6, org.bouncycastle.asn1.x509.KeyUsage.cRLSign),
    encipherOnly(7, org.bouncycastle.asn1.x509.KeyUsage.encipherOnly),
    decipherOnly(8, org.bouncycastle.asn1.x509.KeyUsage.decipherOnly);

    public final int bitPosition;
    public final int bouncyCastleBitPosition;

    KeyUsage(int bitPosition, int bcValue) {
        this.bitPosition = bitPosition;
        bouncyCastleBitPosition = bcValue;
    }

    public static Optional<KeyUsage> byBitPosition(int bitPosition) {
        return Arrays.asList(values()).stream().filter(keyUsage -> keyUsage.bitPosition == bitPosition).findAny();
    }
}
