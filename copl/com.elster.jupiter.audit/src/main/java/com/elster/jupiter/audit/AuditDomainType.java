/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import static com.elster.jupiter.audit.ApplicationType.MDC_APPLICATION_KEY;
import static com.elster.jupiter.audit.ApplicationType.MDM_APPLICATION_KEY;

public enum AuditDomainType {

    UNKNOWN("UNKNOWN", ""),
    DEVICE("DEVICE", MDC_APPLICATION_KEY.getName()),
    USAGEPOINT("USAGEPOINT", MDM_APPLICATION_KEY.getName());

    private final String auditDomainType;
    private final String applicationName;

    AuditDomainType(String auditDomainType, String applicationName) {
        this.auditDomainType = auditDomainType;
        this.applicationName = applicationName;
    }

    public String type() {
        return auditDomainType;
    }
    public String getApplicationName() {
        return applicationName;
    }
}
