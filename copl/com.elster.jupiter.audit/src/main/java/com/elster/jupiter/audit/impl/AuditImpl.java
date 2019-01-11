package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditService;
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

    public enum Field {
        TABLENAME("tableName"),
        REFERENCE("reference"),
        CATEGORY("category"),
        SUBCATEGORY("subCategory"),
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
    private String category;
    private String subCategory;
    private UnexpectedNumberOfUpdatesException.Operation operation;
    private Instant createTime;
    private String userName;

    @Valid
    private List<AuditLog> auditLogs = new ArrayList<>();

    @Inject
    AuditImpl(DataModel dataModel, AuditService auditService) {
        this.dataModel = dataModel;
        this.auditService = auditService;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getOperation() {
        return operation.name();
    }

    @Override
    public Instant getChangedOn() {
        return createTime;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getSubCategory() {
        return subCategory;
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
    public String getName() {
        return ((AuditServiceImpl) auditService)
                .getAuditReferenceResolver(this.tableName)
                .map(auditReferenceResolver -> auditReferenceResolver.getAuditDecoder(reference).getName())
                .orElse("");
    }
}
