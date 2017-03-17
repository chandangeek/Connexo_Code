/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import javax.crypto.SecretKey;

/**
 * Created by bvn on 3/17/17.
 */
public interface PlaintextSymmetricKey extends SymmetricKeyWrapper {
    SecretKey getKey();

    void setKey(SecretKey key);
}
