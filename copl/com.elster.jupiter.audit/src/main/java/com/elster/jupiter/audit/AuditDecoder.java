/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface AuditDecoder {

    String getName();

    Object getReference();

    List<AuditLogChanges> getAuditLogChanges();

}

