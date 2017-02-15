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
     * @param keyAccessorType contains all required information do determine which key to generate (KeyType) and how to store it (KeyEncryptionMethod)
     * @return Persisted PrivateKey container
     */
    PrivateKeyWrapper generatePrivateKey(KeyAccessorType keyAccessorType);

    /**
     * Generates a new symmetrical key
     * @param keyAccessorType contains all required information do determine which key to generate (KeyType) and how to store it (KeyEncryptionMethod)
     * @return Persisted symmetric key container
     */
    SymmetricKeyWrapper generateSymmetricKey(KeyAccessorType keyAccessorType);

}
