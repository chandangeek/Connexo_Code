package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;

/**
 * Created by bvn on 8/8/17.
 */
public class UnsupportedImportOperation extends LocalizedException {
    protected UnsupportedImportOperation(Thesaurus thesaurus, SecurityAccessorType keyAccessorType) {
        super(thesaurus, MessageSeeds.UNSUPPORTED_IMPORT_TYPE, keyAccessorType.getKeyType().getCryptographicType(), keyAccessorType.getName());
    }
}
