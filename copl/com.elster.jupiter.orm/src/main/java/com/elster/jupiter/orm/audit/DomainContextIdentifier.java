package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.TableAudit;

public class DomainContextIdentifier {

    private Long id;
    private Integer domainContext;
    private String reference;
    private int operation;
    private Object object;
    private TableAudit tableAudit;
    private Long reverseReferenceMapValue;

    private long pkDomainColumn;
    private long pkContextColumn1;
    private long pkContextColumn2;

    public DomainContextIdentifier setId(Long id) {
        this.id = id;
        return this;
    }

    public DomainContextIdentifier setDomainContext(Integer domainContext) {
        this.domainContext = domainContext;
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

    public DomainContextIdentifier setPkDomainColumn(long pkDomainColumn) {
        this.pkDomainColumn = pkDomainColumn;
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

    public Integer getDomainContext() {
        return domainContext;
    }

    public String getReference() {
        return reference;
    }

    public int getOperation() {
        return operation;
    }

    public long getPkDomainColumn() {
        return pkDomainColumn;
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

    public long getPkContextColumn1() {
        return pkContextColumn1;
    }

    public long getPkContextColumn2() {
        return pkContextColumn2;
    }

    public DomainContextIdentifier setPkContextColumn1(long pkContextColumn) {
        this.pkContextColumn1 = pkContextColumn;
        return this;
    }

    public DomainContextIdentifier setPkContextColumn2(long pkContextColumn) {
        this.pkContextColumn2 = pkContextColumn;
        return this;
    }
}
