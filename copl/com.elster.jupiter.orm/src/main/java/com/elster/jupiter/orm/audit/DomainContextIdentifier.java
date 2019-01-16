package com.elster.jupiter.orm.audit;

public class DomainContextIdentifier {

    private Long id;
    private String domain;
    private String context;
    private String reference;
    private int operation;

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
}
