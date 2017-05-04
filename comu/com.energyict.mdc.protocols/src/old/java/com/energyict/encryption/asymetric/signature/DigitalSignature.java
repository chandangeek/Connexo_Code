/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption.asymetric.signature;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Implemented by digital signatures.
 *
 * @author alex
 */
public interface DigitalSignature {

    /**
     * Generates a signature for the particular data.
     *
     * @param data The data to sign.
     * @param key  The private key to use.
     * @return The signature.
     */
    byte[] sign(final byte[] data, final PrivateKey key);

    /**
     * Verifies the data was signed with the private key corresponding to the public key.
     *
     * @param data      The data.
     * @param key       The public key to use for verification.
     * @param signature The signature.
     * @return <code>true</code> if the signature is valid, <code>false</code> if it is not.
     */
    boolean verify(final byte[] data, final byte[] signature, final PublicKey key);
}
