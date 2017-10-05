/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.properties.Expiration;

import java.time.Instant;
import java.util.List;

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

    PrivateKeyWrapper newPrivateKeyWrapper(KeyType keyType);

    List<PrivateKeyWrapper> findExpired(Expiration expiration, Instant when);
}
