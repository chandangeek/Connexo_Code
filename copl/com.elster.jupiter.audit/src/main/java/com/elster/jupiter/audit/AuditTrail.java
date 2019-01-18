/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import java.time.Instant;
import java.util.List;

public interface AuditTrail {

    Long getId();

    String getOperation();

    Instant getChangedOn();

    AuditDomainType getDomain();

    AuditDomainContextType getContext();

    String getUser();

    List<AuditLogChanges> getLogs();

    AuditReference getTouchDomain();

    Instant getModTimeStart();

    Instant getModTimeEnd();

    long getPkcolumn();

}
