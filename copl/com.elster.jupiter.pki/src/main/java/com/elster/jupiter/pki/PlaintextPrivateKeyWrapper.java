/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.security.PrivateKey;

/**
 * Interface for plaintext private key wrappers. Unlike other PrivateKeyWrappers, this wrapper allows setting a private key value
 * Created by bvn on 3/2/17.
 */
public interface PlaintextPrivateKeyWrapper extends PrivateKeyWrapper {
    /**
     * read the plaintext private key and stores the (plaintext) encoded value using data vault.
     * @param privateKey The value for this PrivateKeyWrapper
     */
    void setPrivateKey(PrivateKey privateKey);

    void save();
}
