/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Known Extended KeyUsages, provides a mapping to BC KeyPurpose
 * Inspired by sun.security.x509.ExtendedKeyUsageExtension
 */
public enum ExtendedKeyUsage {
    tlsWebServerAuthentication(0, KeyPurposeId.id_kp_serverAuth),
    tlsWebClientAuthentication(1, KeyPurposeId.id_kp_clientAuth),
    digitalSignature(2, KeyPurposeId.id_kp_codeSigning),
    emailProtection(3, KeyPurposeId.id_kp_emailProtection),
    ipSecEndSystem(4, KeyPurposeId.id_kp_ipsecEndSystem),
    ipSecTunnel(5, KeyPurposeId.id_kp_ipsecTunnel),
    ipSecUser(6, KeyPurposeId.id_kp_ipsecUser),
    timeStamping(7, KeyPurposeId.id_kp_timeStamping),
    ocspSigning(8, KeyPurposeId.id_kp_OCSPSigning);

    public final int bitPosition;
    public final KeyPurposeId keyPurposeId;

    private ExtendedKeyUsage(int bitPosition, KeyPurposeId keyPurposeId) {
        this.bitPosition = bitPosition;
        this.keyPurposeId = keyPurposeId;
    }

    public static Optional<ExtendedKeyUsage> byBitPosition(int bitPosition) {
        return Arrays.asList(values()).stream().filter(keyUsage -> keyUsage.bitPosition == bitPosition).findAny();
    }

    public static Optional<ExtendedKeyUsage> byOid(String OID) {
        return Arrays.asList(values()).stream().filter(keyUsage -> keyUsage.keyPurposeId.toOID().getId().equals(OID)).findAny();
    }

    public static EnumSet<ExtendedKeyUsage> fromExtension(Extension extension) {
        if (!extension.getExtnId().getId().equals("2.5.29.37")) {
            throw new IllegalArgumentException("Not a valid extendedKeyUsage extension");
        }
        EnumSet<ExtendedKeyUsage> extendedKeyUsages = EnumSet.noneOf(ExtendedKeyUsage.class);
        for(ASN1Encodable asn1Encodable: ((DLSequence)extension.getParsedValue()).toArray()) {
            byOid(asn1Encodable.toASN1Primitive().toString()).ifPresent(extendedKeyUsages::add);
        }
        return extendedKeyUsages;
    }
}
