/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.utils.krypto;

import com.atos.worldline.jss.api.key.PrivateKey;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.operator.ContentSigner;

import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by H216758 on 10/10/2018.
 */
public class JssContentSignerFactory {

    public static AtomicReference<JssContentSignerFactory> INSTANCE = new AtomicReference<JssContentSignerFactory>(
            new JssContentSignerFactory());

    public ContentSigner getSigner(PrivateKey privateKey, String digestAlgorithm,
                                   AlgorithmIdentifier algorithmIdentifier) throws InvalidKeySpecException {
        if (PKCSObjectIdentifiers.rsaEncryption.equals(algorithmIdentifier.getAlgorithm())) {
            return new JssRsaContentSigner(privateKey, digestAlgorithm);
        } else if (X9ObjectIdentifiers.id_ecPublicKey.equals(algorithmIdentifier.getAlgorithm())) {
            return new JssEcdsaContentSigner(privateKey, digestAlgorithm);
        } else {
            throw new InvalidKeySpecException("unsupported key algorithm: " + algorithmIdentifier);
        }
    }

    public ContentSigner getSigner(PrivateKey privateKey, AlgorithmIdentifier algorithmIdentifier)
            throws InvalidKeySpecException {
        return getSigner(privateKey, "SHA256", algorithmIdentifier);
    }
}
