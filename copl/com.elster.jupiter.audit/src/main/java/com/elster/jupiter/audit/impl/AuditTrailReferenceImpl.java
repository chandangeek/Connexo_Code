/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import java.time.Instant;

public class AuditTrailReferenceImpl implements AuditTrailReference {

    private AuditDomainContextType domainContext;
    private Instant modTimeStart;
    private Instant modTimeEnd;
    private String tableName;
    private long pkDomain;
    private long pkContext1;
    private long pkContext2;
    private UnexpectedNumberOfUpdatesException.Operation operation;


    AuditTrailReferenceImpl() {
    }

    AuditTrailReferenceImpl from(AuditTrail auditTrail) {
        AuditTrailReferenceImpl atr = new AuditTrailReferenceImpl();
        atr.setDomainContext(auditTrail.getDomainContext());
        atr.setModTimeStart(auditTrail.getModTimeStart());
        atr.setModTimeEnd(auditTrail.getModTimeEnd());
        atr.setPkDomain(auditTrail.getPkDomain());
        atr.setPkContext1(auditTrail.getPkContext1());
        atr.setPkContext2(auditTrail.getPkContext2());
        atr.setOperation(auditTrail.getDefaultOperation());
        return atr;
    }

    @Override
    public AuditDomainContextType getDomainContext() {
        return domainContext;
    }

    @Override
    public Instant getModTimeStart() {
        return modTimeStart;
    }

    @Override
    public Instant getModTimeEnd() {
        return modTimeEnd;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public long getPkDomain() {
        return pkDomain;
    }

    @Override
    public long getPkContext1() {
        return pkContext1;
    }

    @Override
    public long getPkContext2() {
        return pkContext2;
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation() {
        return operation;
    }

    public void setDomainContext(AuditDomainContextType domainContext) {
        this.domainContext = domainContext;
    }


    public void setModTimeStart(Instant modTimeStart) {
        this.modTimeStart = modTimeStart;
    }

    public void setModTimeEnd(Instant modTimeEnd) {
        this.modTimeEnd = modTimeEnd;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setPkDomain(long pkDomain) {
        this.pkDomain = pkDomain;
    }

    public void setPkContext1(long pkContext) {
        this.pkContext1 = pkContext;
    }

    public void setPkContext2(long pkContext) {
        this.pkContext2 = pkContext;
    }

    public void setOperation(UnexpectedNumberOfUpdatesException.Operation operation) {
        this.operation = operation;
    }
}
