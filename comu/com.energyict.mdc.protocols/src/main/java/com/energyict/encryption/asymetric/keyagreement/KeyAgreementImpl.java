/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption.asymetric.keyagreement;


import com.energyict.mdc.protocol.api.security.ECCCurve;

import com.energyict.encryption.asymetric.util.KeyUtils;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Key agreement with ephemeral keys.
 *
 * @author alex
 */
public final class KeyAgreementImpl implements KeyAgreement {

    /**
     * ECDH.
     */
    private static final String ECDH = "ECDH";

    /**
     * The public key.
     */
    private final PublicKey ephemeralPublicKey;

    /**
     * The agreement itself.
     */
    private final javax.crypto.KeyAgreement agreement;

    /**
     * Create a new instance.
     *
     * @param curve   The curve to be used.
     * @param keyPair for encryption at the client side: null. An ephemeral key pair will be generated here.
     *                for decryption at the client side: the static client key agreement key pair
     */
    public KeyAgreementImpl(final ECCCurve curve, KeyPair keyPair) {
        try {
            final KeyPair ephemeralKeys = (keyPair != null ? keyPair : KeyUtils.generateECCKeyPair(curve));

            this.ephemeralPublicKey = ephemeralKeys.getPublic();

            this.agreement = javax.crypto.KeyAgreement.getInstance(ECDH);
            this.agreement.init(ephemeralKeys.getPrivate());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error generating ephemeral key pair : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Create a new instance that will generate and use an ephemeral key pair.
     *
     * @param curve The curve to use.
     */
    public KeyAgreementImpl(final ECCCurve curve) {
        this(curve, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final PublicKey getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final byte[] generateSecret(final PublicKey key) {
        try {
            this.agreement.doPhase(Objects.requireNonNull(key), true);

            return this.agreement.generateSecret();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Key agreement failed : [" + e.getMessage() + "]", e);
        }
    }
}
