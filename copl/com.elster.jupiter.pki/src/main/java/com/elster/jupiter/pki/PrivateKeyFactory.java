/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

/**
 * A PrivateKeyFactory allows creation and renewal of keys of a certain KeyEncryptionMethod.
 * Created by bvn on 2/14/17.
 */
public interface PrivateKeyFactory {
    /**
     * Announce which key encryption method this factory supports.
     * @return
     */
    String getKeyEncryptionMethod();

    PrivateKeyWrapper newPrivateKeyWrapper(KeyAccessorType keyAccessorType);
}
