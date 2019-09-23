package com.elster.jupiter.webservices.rest.impl;

public class RelatedObjectInfo {
    public long id;
    public String value;
    public String key;
    public String displayValue;

    public RelatedObjectInfo(long id, String keyDisplayValue){
        this.id = id;
        this.displayValue = keyDisplayValue;
    }
}
