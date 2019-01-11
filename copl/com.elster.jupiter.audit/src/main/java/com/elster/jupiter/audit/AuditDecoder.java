package com.elster.jupiter.audit;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface AuditDecoder {

    String getName();

    List<AuditLogChanges> getAuditLogChanges();

}

