package com.elster.jupiter.webservices.rest.impl;

public class RelatedAttributeInfo {
    public long id;
    public String displayValue;

    public RelatedAttributeInfo(long id, String keyDisplayValue){
        this.id = id;
        this.displayValue = keyDisplayValue;
    }
}
