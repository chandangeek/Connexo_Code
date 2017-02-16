/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.security.NoSuchAlgorithmException;

/**
 * A SymmetricKeyFactory allows creation and renewal of symmetric keys of a certain KeyEncryptionMethod.
 */
public interface SymmetricKeyFactory {
    /**
     * Announce which key encryption method this factory supports.
     * @return
     */
    String getKeyEncryptionMethod();

    SymmetricKeyWrapper newKey(KeyAccessorType keyAccessorType) throws NoSuchAlgorithmException;
}
