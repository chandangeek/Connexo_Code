/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.utils.krypto;

import com.atos.worldline.jss.api.cardmanagement.ECDSASignatureAlgorithm;
import com.atos.worldline.jss.api.cardmanagement.RSASignatureAlgorithm;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

import java.util.List;
import java.util.Set;

/**
 * Created by H216758 on 10/10/2018.
 */
public enum DigestAlgorithmMapping {
    SHA512(RSASignatureAlgorithm.SHA512WithRSAEncryption, ECDSASignatureAlgorithm.ECDSAWithSHA512, PKCSObjectIdentifiers.sha512WithRSAEncryption, X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA-512", "SHA512"),
    SHA384(RSASignatureAlgorithm.SHA384WithRSAEncryption, ECDSASignatureAlgorithm.ECDSAWithSHA384, PKCSObjectIdentifiers.sha384WithRSAEncryption, X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA-384", "SHA384"),
    SHA256(RSASignatureAlgorithm.SHA256WithRSAEncryption, ECDSASignatureAlgorithm.ECDSAWithSHA256, PKCSObjectIdentifiers.sha256WithRSAEncryption, X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA-256", "SHA256"),
    SHA224(RSASignatureAlgorithm.SHA224WithRSAEncryption, ECDSASignatureAlgorithm.ECDSAWithSHA224, PKCSObjectIdentifiers.sha224WithRSAEncryption, X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA-224", "SHA224"),
    SHA1(RSASignatureAlgorithm.SHA1WithRSAEncryption, ECDSASignatureAlgorithm.ECDSAWithSHA1, PKCSObjectIdentifiers.sha1WithRSAEncryption, X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA-1", "SHA1");

    private final RSASignatureAlgorithm rSASignatureAlgorithm;
    private final ECDSASignatureAlgorithm eCDSASignatureAlgorithm;
    private final AlgorithmIdentifier rsaAlgorithmIdentifier;
    private final AlgorithmIdentifier ecdsaAlgorithmIdentifier;
    private final Set<String> names;

    private DigestAlgorithmMapping(final RSASignatureAlgorithm rSASignatureAlgorithm, ECDSASignatureAlgorithm eCDSASignatureAlgorithm, final ASN1ObjectIdentifier rsaAlgorithmIdentifier, ASN1ObjectIdentifier ecdsaAlgorithmIdentifier,
                                   final String name, final String... moreNames) {
        this.rSASignatureAlgorithm = rSASignatureAlgorithm;
        this.eCDSASignatureAlgorithm = eCDSASignatureAlgorithm;
        this.rsaAlgorithmIdentifier = new AlgorithmIdentifier(rsaAlgorithmIdentifier);
        this.ecdsaAlgorithmIdentifier = new AlgorithmIdentifier(ecdsaAlgorithmIdentifier);
        final Set<String> set;
        {
            final List<String> list = Lists.newLinkedList();
            list.add(name);
            for (final String n : moreNames) {
                list.add(n);
            }
            set = ImmutableSet.copyOf(list);
        }
        this.names = set;
    }

    public static final DigestAlgorithmMapping byName(final String digestAlgorithm) {
        for (final DigestAlgorithmMapping mapping : DigestAlgorithmMapping.values()) {
            for (final String name : mapping.names) {
                if (name.equalsIgnoreCase(digestAlgorithm)) {
                    return mapping;
                }
            }
        }
        throw new IllegalArgumentException("unsupported digest algorithm " + digestAlgorithm);
    }

    public RSASignatureAlgorithm getRSASignatureAlgorithm() {
        return rSASignatureAlgorithm;
    }

    public ECDSASignatureAlgorithm getECDSASignatureAlgorithm() {
        return eCDSASignatureAlgorithm;
    }

    public AlgorithmIdentifier getRsaAlgorithmIdentifier() {
        return rsaAlgorithmIdentifier;
    }

    public AlgorithmIdentifier getEcdsaAlgorithmIdentifier() {
        return ecdsaAlgorithmIdentifier;
    }

}
