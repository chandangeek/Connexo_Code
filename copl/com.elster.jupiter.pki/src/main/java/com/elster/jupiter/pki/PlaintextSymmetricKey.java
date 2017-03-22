/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import javax.crypto.SecretKey;
import java.util.Optional;

/**
 * Created by bvn on 3/17/17.
 */
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
}
