/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.nls.TranslationKey;

public enum AuditDomainTranslationKeys implements TranslationKey {

    UNKNOWN(AuditDomainType.UNKNOWN, "Unknown"),
    DEVICE(AuditDomainType.DEVICE, "Device");

    private AuditDomainType key;
    private String defaultFormat;

    AuditDomainTranslationKeys(AuditDomainType key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return this.key.type();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}
