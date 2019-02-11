/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface AuditDecoder {

    String getName();

    Object getContextReference();

    boolean isRemoved();

    default UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return operation;
    }

    List<AuditLogChange> getAuditLogChanges();

}

