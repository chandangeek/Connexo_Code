/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

public enum AuditDomainContextType {

    NODOMAIN("auditDomainContext.noDomainContext", AuditDomainType.UNKNOWN),
    GENERAL_ATTRIBUTES("auditDomainContext.generalAttributes", AuditDomainType.DEVICE),
    DEVICE_ATTRIBUTES("auditDomainContext.deviceAttributes", AuditDomainType.DEVICE),
    DEVICE_CUSTOM_ATTRIBUTES("auditDomainContext.deviceCustomAttributes", AuditDomainType.DEVICE),
    DEVICE_DATA_SOURCE_SPECIFICATIONS("auditDomainContext.deviceDataSourceSpecifications", AuditDomainType.DEVICE),
    DEVICE_CHANNEL_CUSTOM_ATTRIBUTES("auditDomainContext.deviceChannelCustomAttributes", AuditDomainType.DEVICE),
    DEVICE_REGISTER_CUSTOM_ATTRIBUTES("auditDomainContext.deviceRegisterCustomAttributes", AuditDomainType.DEVICE);

    private final String domainContextType;
    private final AuditDomainType domainType;

    AuditDomainContextType(String domainContextType, AuditDomainType domainType) {
        this.domainContextType = domainContextType;
        this.domainType = domainType;
    }

    public String type() {
        return domainContextType;
    }

    public AuditDomainType domainType() {
        return domainType;
    }
}
