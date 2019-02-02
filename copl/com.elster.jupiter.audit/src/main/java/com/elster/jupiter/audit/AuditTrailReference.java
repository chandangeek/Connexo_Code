/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

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

    UnexpectedNumberOfUpdatesException.Operation getOperation();
}

