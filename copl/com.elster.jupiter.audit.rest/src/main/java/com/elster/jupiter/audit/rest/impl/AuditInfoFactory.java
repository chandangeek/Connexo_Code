/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.rest.AuditInfo;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

public class AuditInfoFactory {

    @Inject
    public AuditInfoFactory() {
    }

    public AuditInfo from(Audit audit, Thesaurus thesaurus) {
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.id = audit.getId();
        auditInfo.domain = thesaurus.getString(audit.getDomain().name(), audit.getDomain().name());
        auditInfo.context = thesaurus.getString(audit.getContext().name(), audit.getContext().name());
        auditInfo.domainType = audit.getDomain();
        auditInfo.contextType = audit.getContext();
        auditInfo.changedOn = audit.getChangedOn();
        auditInfo.operation = thesaurus.getString(audit.getOperation(), audit.getOperation());
        auditInfo.operationType = audit.getOperation();
        auditInfo.user = audit.getUser();
        auditInfo.auditReference = new AuditReferenceFactory().from(audit.getTouchDomain());
        auditInfo.auditLogs = audit.getLogs()
                .stream()
                .map(AuditLog::getAuditLogChanges)
                .flatMap(Collection::stream)
                .map(auditLogChanges -> new AuditLogInfoFactory().from(auditLogChanges))
                .sorted((auditLog1, auditLog2) -> auditLog1.getName().compareToIgnoreCase(auditLog2.getName()))
                .collect(Collectors.toList());
        return auditInfo;
    }

    public AuditInfo from(AuditTrail audit, Thesaurus thesaurus) {
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.id = audit.getId();
        auditInfo.domain = thesaurus.getString(audit.getDomain().name(), audit.getDomain().name());
        auditInfo.context = thesaurus.getString(audit.getContext().name(), audit.getContext().name());
        auditInfo.domainType = audit.getDomain();
        auditInfo.contextType = audit.getContext();
        auditInfo.changedOn = audit.getChangedOn();
        auditInfo.operation = thesaurus.getString(audit.getOperation(), audit.getOperation());
        auditInfo.operationType = audit.getOperation();
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