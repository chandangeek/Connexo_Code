package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

/**
 * The KeyService allows generating keys and certificates using registered storage clients (xxxWrapper).
 * The service is intended to be extended by customization code to integrate new Wrappers
 */
@ConsumerType
public interface KeyService {

    /**
     * Generates a new private key
     * @param securityAccessorType contains all required information do determine which key to generate (KeyType) and how to store it (KeyEncryptionMethod)
     * @return Persisted PrivateKey container
     */
    PrivateKeyWrapper generatePrivateKey(SecurityAccessorType securityAccessorType);

    /**
     * Generates a new symmetrical key
     * @param securityAccessorType contains all required information do determine which key to generate (KeyType) and how to store it (KeyEncryptionMethod)
     * @return Persisted symmetric key container
     */
    SymmetricKeyWrapper generateSymmetricKey(SecurityAccessorType securityAccessorType);

}
