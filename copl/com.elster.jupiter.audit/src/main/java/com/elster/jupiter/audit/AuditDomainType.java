/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

public enum AuditDomainType {

    UNKNOWN("UNKNOWN"),
    DEVICE("DEVICE");

    private final String auditDomainType;

    AuditDomainType(String auditDomainType) {
        this.auditDomainType = auditDomainType;
    }

    public String type() {
        return auditDomainType;
    }
}
