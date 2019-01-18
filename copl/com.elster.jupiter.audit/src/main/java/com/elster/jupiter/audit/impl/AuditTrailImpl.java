/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditLogChanges;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditReference;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditTrailImpl implements AuditTrail {

    private DataModel dataModel;
    private AuditService auditService;
    private Thesaurus thesaurus;

    public enum Field {
        TABLENAME("tableName"),
        REFERENCE("reference"),
        SREFERENCE("shortReference"),
        DOMAIN("domain"),
        MODTIMESTART("modTimeStart"),
        MODTIMEEND("modTimeEnd"),
        PKCOLUMN("pkColumn"),
        CONTEXT("context"),
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
    private String tableName;
    private String reference;
    private String shortReference;
    private String domain;
    private String context;
    private UnexpectedNumberOfUpdatesException.Operation operation;
    private Instant createTime;
    private String userName;
    private Instant modTimeStart;
    private Instant modTimeEnd;
    private long pkColumn;

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
    public String getOperation() {
        return getAuditDecoder()
                .map(auditDecoder -> auditDecoder.getOperation(operation, getContext()))
                .map(newOperation -> AuditOperationType.valueOf(newOperation.name()).name())
                .orElseGet(() -> AuditOperationType.valueOf(operation.name()).name());
    }

    @Override
    public Instant getChangedOn() {
        return createTime;
    }

    @Override
    public AuditDomainType getDomain() {
        return AuditDomainType.valueOf(domain);
    }

    @Override
    public AuditDomainContextType getContext() {
        return AuditDomainContextType.valueOf(context);
    }

    @Override
    public String getUser() {
        return userName;
    }

    @Override
    public List<AuditLogChanges> getLogs() {
        return getAuditDecoder()
                .map(auditDecoder -> auditDecoder.getAuditLogChanges())
                .orElse(Collections.emptyList());
    }

    @Override
    public AuditReference getTouchDomain() {
        return getAuditDecoder()
                .map(auditDecoder -> new AuditReferenceImpl(auditDecoder.getName(), auditDecoder.getReference()))
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
    public long getPkcolumn() {
        return pkColumn;
    }

    private Optional<AuditDecoder> getAuditDecoder() {
        return ((AuditServiceImpl) auditService)
                .getAuditTrailDecoderHandles(this.domain, this.context)
                .map(auditReferenceResolver -> auditReferenceResolver.getAuditDecoder(new AuditTrailReferenceImpl().from(this)));
    }
}
