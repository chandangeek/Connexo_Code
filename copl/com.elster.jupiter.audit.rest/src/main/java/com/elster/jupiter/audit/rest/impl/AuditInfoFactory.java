/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.rest.AuditInfo;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class AuditInfoFactory {

    @Inject
    public AuditInfoFactory() {
    }

    public AuditInfo from(AuditTrail audit, Thesaurus thesaurus) {
        AuditInfo auditInfo = new AuditInfo();
        AuditDomainContextType auditDomainContextType = (audit.getOperation() == AuditOperationType.UPDATE) ?
                audit.getContext() : AuditDomainContextType.EMPTY;
        auditInfo.id = audit.getId();
        auditInfo.domain = thesaurus.getString(audit.getDomain().name(), audit.getDomain().name());
        auditInfo.context = thesaurus.getString(auditDomainContextType.name(), auditDomainContextType.name());
        auditInfo.contextType = auditDomainContextType;
        auditInfo.domainType = audit.getDomain();
        auditInfo.changedOn = audit.getChangedOn();
        auditInfo.operation = thesaurus.getString(audit.getOperation().name(), audit.getOperation().name());
        auditInfo.operationType = audit.getOperation().name();
        auditInfo.user = audit.getUser();
        auditInfo.auditReference = new AuditReferenceFactory().from(audit.getTouchDomain());
        auditInfo.auditLogs = audit.getLogs()
                .stream()
                .map(auditLogChanges -> new AuditLogInfoFactory().from(auditLogChanges))
                .sorted((auditLog1, auditLog2) -> auditLog1.getName().compareToIgnoreCase(auditLog2.getName()))
                .collect(Collectors.toList());
        return auditInfo;
    }
}