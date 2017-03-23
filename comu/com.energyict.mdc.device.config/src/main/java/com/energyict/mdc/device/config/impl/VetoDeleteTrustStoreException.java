/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.TrustStore;

public class VetoDeleteTrustStoreException extends LocalizedException {

    public VetoDeleteTrustStoreException(Thesaurus thesaurus, TrustStore trustStore) {
        super(thesaurus, MessageSeeds.VETO_TRUSTSTORE_DELETION, trustStore.getName());
    }

}