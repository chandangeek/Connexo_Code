/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.nls.TranslationKey;

public enum AuditDomainContextTranslationKeys implements TranslationKey {

    UNKNOWN(AuditDomainContextType.EMPTY, ""),
    GENERAL_ATTRIBUTES(AuditDomainContextType.GENERAL_ATTRIBUTES, "General attributes"),
    DEVICE_ATTRIBUTES(AuditDomainContextType.DEVICE_ATTRIBUTES, "Device attributes");

    private AuditDomainContextType key;
    private String defaultFormat;

    AuditDomainContextTranslationKeys(AuditDomainContextType key, String defaultFormat) {
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
