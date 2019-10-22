/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import static com.elster.jupiter.audit.ApplicationType.MDC_APPLICATION_KEY;
import static com.elster.jupiter.audit.ApplicationType.MDM_APPLICATION_KEY;
import static com.elster.jupiter.audit.ApplicationType.UNKNOWN_APPLICATION_KEY;

public enum AuditDomainType {

    UNKNOWN("UNKNOWN", UNKNOWN_APPLICATION_KEY),
    DEVICE("DEVICE", MDC_APPLICATION_KEY),
    USAGEPOINT("USAGEPOINT", MDM_APPLICATION_KEY);

    private final String auditDomainType;
    private final ApplicationType applicationType;

    AuditDomainType(String auditDomainType, ApplicationType applicationType) {
        this.auditDomainType = auditDomainType;
        this.applicationType = applicationType;
    }

    public String type() {
        return auditDomainType;
    }
    public ApplicationType getApplicationType() {
        return applicationType;
    }
}
