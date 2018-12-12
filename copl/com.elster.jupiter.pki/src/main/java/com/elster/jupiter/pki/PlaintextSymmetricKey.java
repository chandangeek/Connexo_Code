/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;

import javax.crypto.SecretKey;
import java.util.Optional;

/**
 * Created by bvn on 3/17/17.
 */
@ProviderType
public interface PlaintextSymmetricKey extends SymmetricKeyWrapper {
    /**
     * Plaintext keys expose the actual secret key. As the word implies, this key is intended to be kept secret.
     * @return If the wrapper contains a key, the key is returned, if not, Optional.empty()
     */
    Optional<SecretKey> getKey();

    /**
     * Set the plaintxt value of the key. The key will be encrypted prior to storage in the db.
     * @param key The plaintext SecretKey to store.
     */
    void setKey(SecretKey key);


    /**
     * Allows the generation of a random value for an empty wrapper, in this case, a symmetric key.
     * Any existing value will be overwritten.
     * It's up to the implementing class to make sure all renewal information is available (through linking
     * KeyTypes/KeyAccessorTypes)
     * Note that not all key encryption methods will permit automatic renewal.
     */
    void generateValue();
}
