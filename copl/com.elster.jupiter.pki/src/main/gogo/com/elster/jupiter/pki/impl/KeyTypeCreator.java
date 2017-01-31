package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;

/**
 * Interface to facilitate creation of KeyTypes from enum
 */
public interface KeyTypeCreator {
    public String getName();
    public KeyType createKeyType(PkiService pkiService);
}
