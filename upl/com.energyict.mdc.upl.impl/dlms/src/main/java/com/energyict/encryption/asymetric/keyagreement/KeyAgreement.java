package com.energyict.encryption.asymetric.keyagreement;

import java.security.PublicKey;

/**
 * Implements the key agreement (ECDH) procedure.
 *
 * @author alex
 */
public interface KeyAgreement {

    /**
     * Returns the public key to be used in the exchange.
     *
     * @return The public key to be used in the exchange.
     */
    PublicKey getPublicKey();

    /**
     * Generates the secret.
     *
     * @param key The public key of the other party.
     * @return The generated secret.
     */
    byte[] generateSecret(final PublicKey key);
}
