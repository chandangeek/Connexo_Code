/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditReference;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AuditImpl implements Audit {

    private DataModel dataModel;
    private AuditService auditService;
    private Thesaurus thesaurus;

    public enum Field {
        TABLENAME("tableName"),
        REFERENCE("reference"),
        SREFERENCE("shortReference"),
        DOMAIN("domain"),
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

    @Valid
    private List<AuditLog> auditLogs = new ArrayList<>();

    @Inject
    AuditImpl(DataModel dataModel, AuditService auditService, Thesaurus thesaurus) {
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
        return AuditOperationType.valueOf(operation.name()).name();
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
    public List<AuditLog> getLogs() {
        return auditLogs;
    }

    @Override
    public AuditReference getTouchDomain() {
        return ((AuditServiceImpl) auditService)
                .getAuditDecoderHandles(this.tableName)
                .map(auditReferenceResolver -> auditReferenceResolver.getAuditDecoder(reference))
                .map(auditDecoder -> new AuditReferenceImpl(auditDecoder.getName(), auditDecoder.getReference()))
                .orElse(new AuditReferenceImpl());
    }
}
