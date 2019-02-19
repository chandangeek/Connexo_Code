/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.nls.TranslationKey;

public enum AuditDomainContextTranslationKeys implements TranslationKey {

    NODOMAIN(AuditDomainContextType.NODOMAIN, ""),
    GENERAL_ATTRIBUTES(AuditDomainContextType.GENERAL_ATTRIBUTES, "General attributes"),
    DEVICE_ATTRIBUTES(AuditDomainContextType.DEVICE_ATTRIBUTES, "Device attributes"),
    DEVICE_CUSTOM_ATTRIBUTES(AuditDomainContextType.DEVICE_CUSTOM_ATTRIBUTES, "Device attributes"),
    DEVICE_CHANNEL_SPECIFICATIONS(AuditDomainContextType.DEVICE_CHANNEL_SPECIFICATIONS, "Channel specifications"),
    DEVICE_CHANNEL_CUSTOM_ATTRIBUTES(AuditDomainContextType.DEVICE_CHANNEL_CUSTOM_ATTRIBUTES, "Channel"),
    DEVICE_REGISTER_SPECIFICATIONS(AuditDomainContextType.DEVICE_REGISTER_SPECIFICATIONS, "Register specifications"),
    DEVICE_REGISTER_CUSTOM_ATTRIBUTES(AuditDomainContextType.DEVICE_REGISTER_CUSTOM_ATTRIBUTES, "Register")
    ;

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
