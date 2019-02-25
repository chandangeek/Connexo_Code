/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditReference;
import com.elster.jupiter.audit.rest.AuditReferenceInfo;

public class AuditReferenceFactory {

    public AuditReferenceFactory() {
    }

    public AuditReferenceInfo from(AuditReference auditReference) {
        AuditReferenceInfo auditReferenceInfo = new AuditReferenceInfo();
        auditReferenceInfo.setName(auditReference.getName());
        auditReferenceInfo.setContextReference(auditReference.getContextReference());
        auditReferenceInfo.setRemoved(auditReference.isRemoved());
        return auditReferenceInfo;
    }
}