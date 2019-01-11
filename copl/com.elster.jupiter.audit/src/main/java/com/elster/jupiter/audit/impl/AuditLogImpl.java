package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditLogChanges;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class AuditLogImpl implements AuditLog {

    private DataModel dataModel;
    private AuditService auditService;

    public enum Field {
        TABLENAME("tableName"),
        REFERENCE("reference"),
        AUDIT("audit");

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
    private Reference<Audit> audit = ValueReference.absent();

    @Inject
    AuditLogImpl(DataModel dataModel, AuditService auditService) {
        this.dataModel = dataModel;
        this.auditService = auditService;
    }

    @Override
    public String getRecord() {
        return reference;
    }

    @Override
    public String getName() {
        return ((AuditServiceImpl) auditService)
                .getAuditReferenceResolver(this.tableName)
                .map(auditReferenceResolver -> auditReferenceResolver.getAuditDecoder(reference).getName())
                .orElse("");
    }

    @Override
    public List<AuditLogChanges> getAuditLogChanges() {
        return ((AuditServiceImpl) auditService)
                .getAuditReferenceResolver(this.tableName)
                .map(auditReferenceResolver -> auditReferenceResolver.getAuditDecoder(reference).getAuditLogChanges())
                .orElse(Collections.emptyList());
    }
}
