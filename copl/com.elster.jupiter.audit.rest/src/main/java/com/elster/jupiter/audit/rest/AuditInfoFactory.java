/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditLog;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

public class AuditInfoFactory {

    @Inject
    public AuditInfoFactory() {
    }

    public AuditInfo from(Audit audit, AuditLogInfoFactory auditLogInfoFactory) {
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.id = audit.getId();
        auditInfo.category = audit.getCategory();
        auditInfo.subCategory = audit.getSubCategory();
        auditInfo.changedOn = audit.getChangedOn();
        auditInfo.operation = audit.getOperation();
        auditInfo.user = audit.getUser();
        auditInfo.name = audit.getName();
        auditInfo.auditLogs = audit.getLogs()
                .stream()
                .map(AuditLog::getAuditLogChanges)
                .flatMap(Collection::stream)
                .map(auditLogChanges -> auditLogInfoFactory.from(auditLogChanges))
                .sorted((auditLog1, auditLog2) -> auditLog1.getName().compareToIgnoreCase(auditLog2.getName()))
                .collect(Collectors.toList());
        return auditInfo;
    }
}