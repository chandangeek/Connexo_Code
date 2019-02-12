/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.rest.AuditLogInfo;

import javax.inject.Inject;

public class AuditLogInfoFactory {

    @Inject
    public AuditLogInfoFactory() {
    }

    public AuditLogInfo from(AuditLogChange auditLogChanges) {
        AuditLogInfo auditLogInfo = new AuditLogInfo();
        auditLogInfo.setName(auditLogChanges.getName());
        auditLogInfo.setValue(auditLogChanges.getValue());
        auditLogInfo.setPreviousValue(auditLogChanges.getPreviousValue());
        auditLogInfo.setType(auditLogChanges.getType());
        return auditLogInfo;
    }
}