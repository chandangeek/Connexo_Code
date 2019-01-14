/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditReference;

public class AuditReferenceImpl implements AuditReference {

    private String name;
    private Object reference;

    AuditReferenceImpl() {
    }

    AuditReferenceImpl(String name, Object reference) {
        this.name = name;
        this.reference = reference;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getReference() {
        return reference;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReference(Object value) {
        this.reference = value;
    }
}
