/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

public enum AuditDomainContextType {

    UNKNOWN("UNKNOWN", AuditDomainType.UNKNOWN),
    GENERAL_ATTRIBUTES("GENERAL_ATTRIBUTES", AuditDomainType.DEVICE),
    DEVICE_ATTRIBUTES("DEVICE_ATTRIBUTES", AuditDomainType.DEVICE);

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
