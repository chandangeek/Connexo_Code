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
    DEVICE_DATA_SOURCE_SPECIFICATIONS(AuditDomainContextType.DEVICE_DATA_SOURCE_SPECIFICATIONS, "Data source specifications"),
    DEVICE_CHANNEL_CUSTOM_ATTRIBUTES(AuditDomainContextType.DEVICE_CHANNEL_CUSTOM_ATTRIBUTES, "Channel"),
    DEVICE_REGISTER_CUSTOM_ATTRIBUTES(AuditDomainContextType.DEVICE_REGISTER_CUSTOM_ATTRIBUTES, "Register"),
    DEVICE_PROTOCOL_DIALECTS_PROPS(AuditDomainContextType.DEVICE_PROTOCOL_DIALECTS_PROPS, "Protocol Dialects");
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
