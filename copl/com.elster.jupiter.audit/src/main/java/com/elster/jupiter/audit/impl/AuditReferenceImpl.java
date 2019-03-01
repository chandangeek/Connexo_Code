/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditReference;

public class AuditReferenceImpl implements AuditReference {

    private AuditDecoder auditDecoder;

    public AuditReferenceImpl from(AuditDecoder auditDecoder) {
        this.auditDecoder = auditDecoder;
        return this;
    }

    @Override
    public String getName() {
        return auditDecoder.getName();
    }

    @Override
    public Object getContextReference() {
        return auditDecoder.getContextReference();
    }

    @Override
    public boolean isRemoved() {
        return auditDecoder.isRemoved();
    }

}
