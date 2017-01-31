package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;

/**
 * Created by bvn on 1/31/17.
 */
public interface KeyTypeCreator {
    public String getName();
    public KeyType createKeyType(PkiService pkiService);
}
