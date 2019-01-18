/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface AuditTrailReference {

    AuditDomainType getDomain();

    AuditDomainContextType getContext();

    Instant getModTimeStart();

    Instant getModTimeEnd();

    String getTableName();

    long getPkcolumn();

    String getOperation();
}

