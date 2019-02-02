/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import java.time.Instant;
import java.util.List;

public interface AuditTrail {

    Long getId();

    AuditOperationType getOperation();

    UnexpectedNumberOfUpdatesException.Operation getDefaultOperation();

    Instant getChangedOn();

    AuditDomainType getDomain();

    AuditDomainContextType getContext();

    String getUser();

    List<AuditLogChange> getLogs();

    AuditReference getTouchDomain();

    Instant getModTimeStart();

    Instant getModTimeEnd();

    long getPkcolumn();

}
