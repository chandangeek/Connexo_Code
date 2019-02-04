package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.TableAudit;

public class DomainContextIdentifier {

    private Long id;
    private String domain;
    private String context;
    private String reference;
    private int operation;
    private Object object;
    private TableAudit tableAudit;
    private Long reverseReferenceMapValue;

    private long pkColumn;

    public DomainContextIdentifier setId(Long id) {
        this.id = id;
        return this;
    }

    public DomainContextIdentifier setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public DomainContextIdentifier setContext(String context) {
        this.context = context;
        return this;
    }

    public DomainContextIdentifier setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public DomainContextIdentifier setOperation(int operation) {
        this.operation = operation;
        return this;
    }

    public DomainContextIdentifier setPkColumn(long pkColumn) {
        this.pkColumn = pkColumn;
        return this;
    }

    public DomainContextIdentifier setObject(Object object) {
        this.object = object;
        return this;
    }

    public DomainContextIdentifier setTableAudit(TableAudit tableAudit) {
        this.tableAudit = tableAudit;
        return this;
    }

    public DomainContextIdentifier setReverseReferenceMapValue(Long reverseReferenceMapValue) {
        this.reverseReferenceMapValue = reverseReferenceMapValue;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getContext() {
        return context;
    }

    public String getReference() {
        return reference;
    }

    public int getOperation() {
        return operation;
    }

    public long getPkColumn() {
        return pkColumn;
    }

    public Object getObject() {
        return object;
    }

    public TableAudit getTableAudit() {
        return tableAudit;
    }

    public Long getReverseReferenceMapValue() {
        return reverseReferenceMapValue;
    }
}
