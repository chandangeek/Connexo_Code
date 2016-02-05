package com.energyict.encryption.asymetric.keyagreement;

import java.security.PublicKey;

/**
 * Implements the key agreement (ECDH) procedure.
 *
 * @author alex
 */
public interface KeyAgreement {

    /**
     * Returns our ephemeral public key that the server should use for the key agreement.
     */
    PublicKey getEphemeralPublicKey();

    /**
     * Generates the secret.
     *
     * @param key The public key of the other party.
     * @return The generated secret.
     */
    byte[] generateSecret(final PublicKey key);
}
