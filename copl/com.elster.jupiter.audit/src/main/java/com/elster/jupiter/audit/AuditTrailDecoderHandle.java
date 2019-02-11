/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface AuditTrailDecoderHandle {

    List<String> getPrivileges();

    AuditDecoder getAuditDecoder(AuditTrailReference reference);

    AuditDomainContextType getAuditDomainContextType();


}
