/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.pki.SymmetricKeyWrapper;

import javax.crypto.SecretKey;

/**
 * Created by bvn on 2/16/17.
 */
public class SymmetricKeyWrapperImpl implements SymmetricKeyWrapper {
    @Override
    public String getKeyEncryptionMethod() {
        return PlaintextSymmetricKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    @Override
    public SecretKey getKey() {
        return null;
    }

    protected void save() {

    }
}
