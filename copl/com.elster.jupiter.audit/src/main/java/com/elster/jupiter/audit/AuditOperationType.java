/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

public enum AuditOperationType {

    INSERT(UnexpectedNumberOfUpdatesException.Operation.INSERT.name()),
    UPDATE(UnexpectedNumberOfUpdatesException.Operation.UPDATE.name()),
    DELETE(UnexpectedNumberOfUpdatesException.Operation.DELETE.name());

    private final String auditDomainType;

    AuditOperationType(String auditDomainType) {
        this.auditDomainType = auditDomainType;
    }

    public String type() {
        return auditDomainType;
    }
}
