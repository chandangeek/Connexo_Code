package com.elster.jupiter.orm.audit;

public class DomainContextIdentifier {

    private Long id;
    private String domain;
    private String context;
    private String reference;
    private int operation;

    private String pkColumn1;
    private String pkColumn2;
    private String pkColumn3;

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

    public String getPkColumn1() {
        return pkColumn1;
    }

    public DomainContextIdentifier setPkColumn1(String pkColumn1) {
        this.pkColumn1 = pkColumn1;
        return this;
    }

    public String getPkColumn2() {
        return pkColumn2;
    }

    public DomainContextIdentifier setPkColumn2(String pkColumn2) {
        this.pkColumn2 = pkColumn2;
        return this;
    }

    public String getPkColumn3() {
        return pkColumn3;
    }

    public DomainContextIdentifier setPkColumn3(String pkColumn3) {
        this.pkColumn3 = pkColumn3;
        return this;
    }
}
