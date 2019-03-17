/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditReference;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AuditTrailImpl implements AuditTrail {

    private DataModel dataModel;
    private AuditService auditService;
    private Thesaurus thesaurus;
    private List<AuditDecoder> auditDecoders = new ArrayList<>();

    public enum Field {
        REFERENCE("reference"),
        SREFERENCE("shortReference"),
        DOMAINCONTEXT("domainContext"),
        MODTIMESTART("modTimeStart"),
        MODTIMEEND("modTimeEnd"),
        PKDOMAIN("pkDomain"),
        PKCONTEXT1("pkContext1"),
        PKCONTEXT2("pkContext2"),
        OPERATION("operation"),
        CREATETIME("createTime"),
        USERNAME("userName");

        private final String javaFieldName;

        private Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String reference;
    private String shortReference;
    private AuditDomainContextType domainContext;
    private UnexpectedNumberOfUpdatesException.Operation operation;
    private Instant createTime;
    private String userName;
    private Instant modTimeStart;
    private Instant modTimeEnd;
    private long pkDomain;
    private long pkContext1;
    private long pkContext2;

    @Inject
    AuditTrailImpl(DataModel dataModel, AuditService auditService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.auditService = auditService;
        this.thesaurus = thesaurus;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public AuditOperationType getOperation() {
        return getAuditDecoders()
                .stream()
                .findFirst()
                .map(auditDecoder -> auditDecoder.getOperation(operation, getDomainContext()))
                .map(newOperation -> AuditOperationType.valueOf(newOperation.name()))
                .orElseGet(() -> AuditOperationType.valueOf(operation.name()));
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getDefaultOperation() {
        return operation;
    }

    @Override
    public Instant getChangedOn() {
        return createTime;
    }

    @Override
    public AuditDomainContextType getDomainContext() {
        return domainContext;
    }

    @Override
    public String getUser() {
        return userName;
    }

    @Override
    public List<AuditLogChange> getLogs() {
        return getAuditDecoders()
                .stream()
                .map(auditDecoder -> auditDecoder.getAuditLogChanges())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public AuditReference getTouchDomain() {
        return getAuditDecoders()
                .stream()
                .findFirst()
                .map(auditDecoder -> new AuditReferenceImpl().from(auditDecoder))
                .orElse(new AuditReferenceImpl());
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

    private List<AuditDecoder> getAuditDecoders() {
        if (auditDecoders.size() == 0) {
            auditDecoders = ((AuditServiceImpl) auditService)
                    .getAuditTrailDecoderHandles(domainContext)
                    .stream()
                    .map(auditReferenceResolver -> auditReferenceResolver.getAuditDecoder(new AuditTrailReferenceImpl().from(this)))
                    .collect(Collectors.toList());
        }
        return auditDecoders;
    }
}
