/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.AuditTrailReference;

import java.time.Instant;

public class AuditTrailReferenceImpl implements AuditTrailReference {

    private AuditDomainType domain;
    private AuditDomainContextType context;
    private Instant modTimeStart;
    private Instant modTimeEnd;
    private String tableName;
    private String pkcolumn1;
    private String pkcolumn2;
    private String pkcolumn3;
    private String operation;


    AuditTrailReferenceImpl() {
    }

    AuditTrailReferenceImpl from(AuditTrail auditTrail) {
        AuditTrailReferenceImpl atr = new AuditTrailReferenceImpl();
        atr.setDomain(auditTrail.getDomain());
        atr.setContext(auditTrail.getContext());
        atr.setModTimeStart(auditTrail.getModTimeStart());
        atr.setModTimeEnd(auditTrail.getModTimeEnd());
        atr.setPkcolumn1(auditTrail.getPkcolumn1());
        atr.setPkcolumn2(auditTrail.getPkcolumn2());
        atr.setPkcolumn3(auditTrail.getPkcolumn3());
        //atr.setOperation(auditTrail.getOperation());
        return atr;
    }

    @Override
    public AuditDomainType getDomain() {
        return domain;
    }

    @Override
    public AuditDomainContextType getContext() {
        return context;
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
    public String getPkcolumn1() {
        return pkcolumn1;
    }

    @Override
    public String getPkcolumn2() {
        return pkcolumn2;
    }

    @Override
    public String getPkcolumn3() {
        return pkcolumn3;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    public void setDomain(AuditDomainType domain) {
        this.domain = domain;
    }

    public void setContext(AuditDomainContextType context) {
        this.context = context;
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

    public void setPkcolumn1(String pkcolumn1) {
        this.pkcolumn1 = pkcolumn1;
    }

    public void setPkcolumn2(String pkcolumn2) {
        this.pkcolumn2 = pkcolumn2;
    }

    public void setPkcolumn3(String pkcolumn3) {
        this.pkcolumn3 = pkcolumn3;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
